package com.vdevcode.guardian.helpers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.vdevcode.guardian.models.Command
import com.vdevcode.guardian.repo.AppRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.Normalizer


/**
 * Classe reponsável pelos metodos utilitarios para a aplicação.
 */
object Helper {

    private fun log(msg: String, type: Int) = when (type) {
        Log.ERROR -> Log.e(ConstantHelper.APP_LOG_TAG, msg)
        Log.INFO -> Log.i(ConstantHelper.APP_LOG_TAG, msg)
        Log.WARN -> Log.w(ConstantHelper.APP_LOG_TAG, msg)
        else -> Log.d(ConstantHelper.APP_LOG_TAG, msg)
    }

    fun LogE(msg: String) = log(msg, Log.ERROR)
    fun LogI(msg: String) = log(msg, Log.INFO)
    fun LogW(msg: String) = log(msg, Log.WARN)


    fun removeAccent(message: String): String {
        return Normalizer.normalize(message, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
    }

    fun normalizeText(text: String?): String {
        text?.let {
            val norm = removeAccent(text).toLowerCase().trim().replace("\\s+".toRegex(), " ")
            return norm.replace("[^A-Za-z ]".toRegex(), " ")
        }
        return ""
    }


    // fun addCommands() {
    val commands = arrayOf(
        "apavorado", "aqui", "baixo", "bem", "boa", "calma", "calmo", "carro", "casa", "cedo", "celular", "de", "deixa", "deixando", "deixar", "deus", "dia", "dinheiro", "documento", "em", "embaixo", "encostado", "entregando", "entregar", "estamos",
        "estava", "estou", "familia", "favor", "fazer", "ficamos", "filhas", "filhos", "fiquei", "fiquem", "fiz", "fizemos", "igreja", "indo", "ir", "levantando", "levantar", "levar", "machuca", "machuque", "mal", "me", "medo", "meu", "meus"
    )
    /* GlobalScope.launch(Dispatchers.IO) {
         commands.forEach {
             AppRepo.insert(Command().apply {
                 palavra = it
             })
         }
     }
     */
    // }

}