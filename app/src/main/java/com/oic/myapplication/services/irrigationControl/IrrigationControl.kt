package com.oic.myapplication.services.irrigationControl

import android.content.Context
import android.util.Log
import com.oic.myapplication.helper.MyUrlRequestCallback
import com.oic.myapplication.helper.random_joke
import org.chromium.net.CronetEngine
import org.chromium.net.UrlRequest
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class IrrigationControl(context: Context) {
    /* Where both "hardware" and database connectivity happens */
    val executor: Executor = Executors.newSingleThreadExecutor()
    val context = context

    /* when user wants to manually start irrigation on all zones */
    fun startAllZones(){
        //assuming an already running valve does nothing if received this request
        try {
            val myBuilder = CronetEngine.Builder(context)
            val cronetEngine: CronetEngine = myBuilder.build()
            val api = runAllZonesAPI()
            val requestBuilder = cronetEngine.newUrlRequestBuilder(
                api,
                MyUrlRequestCallback(),
                executor
            )
            val request: UrlRequest = requestBuilder.build()
            request.start()
        } catch (e: Exception){
            Log.e("IRRIGATION CONTROL", "Error:", e)
        }
    }

    fun testConnectivity(){
        try {
            val myBuilder = CronetEngine.Builder(context)
            val cronetEngine: CronetEngine = myBuilder.build()
            val api = random_joke()
            val requestBuilder = cronetEngine.newUrlRequestBuilder(
                api,
                MyUrlRequestCallback(),
                executor
            )
            val request: UrlRequest = requestBuilder.build()
            request.start()
        } catch (e: Exception){
            Log.e("IRRIGATION CONTROL", "Error:", e)
        }
    }
}