package com.example.speech_textime.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.provider.MediaStore
import java.io.File

class PressHoldRecorder(private val context : Context) {

    private var recorder : MediaRecorder? =null
    private var  outFile : File? = null

    fun start() : File{
           stop()

        val file = File.createTempFile("ime_rec_" , ".m4a" ,context.cacheDir)
             outFile = file

        val r = if(Build.VERSION.SDK_INT >= 31 ) MediaRecorder(context) else MediaRecorder()
          recorder = r.apply {
              setAudioSource(MediaRecorder.AudioSource.MIC)
              setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
              setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
              setAudioEncodingBitRate(128_000)
              setAudioSamplingRate(48_000)
              setOutputFile(file.absolutePath)
              prepare()
              start()
          }
        return file
    }

    fun stop() : File?{
        return  try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
            recorder = null
            outFile
        }
        catch (_: Exception){
            recorder = null
            null
        }
    }

    fun discard(){
        stop()?.delete()
        outFile = null
    }


}