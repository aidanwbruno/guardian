package com.vdevcode.guardion.helpers

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.vdevcode.guardian.auth.AppAuth
import com.vdevcode.guardian.database.AppFireDB
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object AudioFileHelper {

    const val AUDIOS_PATH = "audios"
    //const val EPISODE_PATH = "s% histories"
    var mediaPlayer: MediaPlayer? = MediaPlayer()
    var mediaRecorder: MediaRecorder? = MediaRecorder()
    var currentAudio: File? = null
    var playing = false
    var created = false
    var recording = false
    private fun fireStorage() = FirebaseStorage.getInstance().reference

    fun startRecordAudio(context: Context, onAudioRecoded: () -> Unit) {
        var audioPath: File?
        stopRecording()
        delete()
        val time = SimpleDateFormat("ddMMyyyyHHmm").format(Date())
        val audioName = if (AppAuth.getUserId().isNullOrBlank()) "AUD_NO_USER".plus("_" + time) else "AUD_".plus(AppAuth.getUserId()).plus("_" + time)
        try {
            audioPath = FileHelper.createFilesDir(context, AUDIOS_PATH) //ChatFileUpload.createTempFile(getContext(), null, ChatFileUpload.VOICE_SENT_FOLDER, ChatHelper.FILE_VOI_PREFIX, ".mp3", null)
            val fileAudio = File("$audioPath${File.separator}$audioName.mp3")
            if (fileAudio.createNewFile()) {
                currentAudio = fileAudio
            } else {
                currentAudio = File.createTempFile(audioName, ".mp3", audioPath)
            }

        } catch (ex: IOException) {
            Guardian.toast("Falha ao gravar Audio")
        }
        if (currentAudio == null) {
            Guardian.toast("Falha ao gravar Audio")
        } else {
            try {
                created = false
                recording = true
                if (mediaRecorder == null) {
                    mediaRecorder = MediaRecorder()
                }
                try {
                    mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                    // mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    mediaRecorder?.setOutputFile(currentAudio?.absolutePath)
                    mediaRecorder?.setMaxDuration(200000)
                    mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    //mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    mediaRecorder?.setMaxFileSize(1000000L) // 1 mega pra teste

                    try {
                        mediaRecorder?.prepare()
                    } catch (e: Exception) {
                        recording = false
                        stopRecording()
                        delete()
                        Guardian.toast("Falha ao Preparar Audio")
                    }
                    if (recording) {
                        mediaRecorder?.start()
                        Guardian.toast("Gravando Audio...")
                    }
                } catch (e: Exception) {
                    recording = false
                    stopRecording()
                    delete()
                    Guardian.toast("Falha ao Preparar Audio")
                }
                Handler().postDelayed({
                    Guardian.toast("Áudio Gravado com Sucesso!!")
                    stopRecording()
                    uploadToFireStorage(audioName)
                    recording = false
                    onAudioRecoded.invoke()
                }, 31000)
                //isRecording = true
                //mCurrentAudioPath = currentAudio.absolutePath

            } catch (e: IOException) {
                Helper.LogE("AUDIO RECORD ERRO")
                FileHelper.deleteFile(currentAudio!!)
                recording = false
            }
        }
    }


    fun stopRecording() {
        try {
            mediaRecorder?.run {
                stop()
                release()
                mediaRecorder = null
                Guardian.toast("Audio Gravado")
            }
            stopAudio()
        } catch (ex: Exception) {
            mediaRecorder?.run {
                release()
                mediaRecorder = null
                Guardian.toast("Audio Gravado")
            }
        }
        recording = false
    }

    fun playAudio(context: Context, audio: String?) {
        audio?.let {
            try {
                mediaPlayer?.run {
                    setDataSource(FileProvider.getUriForFile(context, "com.vdevcode.guardian.fileprovider", File(it)).path)
                    prepare()
                    setOnPreparedListener {
                        it.start()
                        playing = true
                    }
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.run {
            stop()
            release()
            mediaPlayer = null
            playing = false
        }
    }

    fun delete() {
        currentAudio?.let {
            FileHelper.deleteFile(it)
            currentAudio = null
            recording = false
            created = false
        }
    }

    fun uploadToFireStorage(audioName: String) {
        currentAudio?.let {
            val audioRef = fireStorage().child(audioName)
            val task = audioRef.putStream(currentAudio?.inputStream()!!)
            task.addOnFailureListener {
                Guardian.toast("Erro ao Enviar Audio ao Firebase")
            }.addOnSuccessListener {
                Guardian.toast("Áudio enviado com sucesso para Firebase")
                audioRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        AppFireDB.updateCurrentAlert(it.result.toString()) // update the audio
                    } else {
                        AppFireDB.updateCurrentAlert(audioName) // update the audio
                    }
                }
            }
        }
    }

    fun isFileOk(): Boolean {
        currentAudio?.let {
            return true
        }
        return false
    }

}
