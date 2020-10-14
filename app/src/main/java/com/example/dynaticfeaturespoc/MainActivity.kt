package com.example.dynaticfeaturespoc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.android.play.core.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mySessionId = 0
    lateinit var splitInstallManager: SplitInstallManager

    private val stateInstallListener: SplitInstallStateUpdatedListener =
        SplitInstallStateUpdatedListener { state ->
            if (state.status() == SplitInstallSessionStatus.FAILED
                && state.errorCode() == SplitInstallErrorCode.SERVICE_DIED
            ) {

                return@SplitInstallStateUpdatedListener
            }

            if (state.sessionId() == mySessionId) {
                when (state.status()) {
                    SplitInstallSessionStatus.DOWNLOADING -> {
                        val totalBytes = state.totalBytesToDownload().toInt()
                        val progress = state.bytesDownloaded().toInt()
                        displayToast("LISTENER Downloading")
                    }
                    SplitInstallSessionStatus.INSTALLED -> {
                        displayToast("LISTENER Installed")
                    }
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                        splitInstallManager.startConfirmationDialogForResult(state, this, 1)
                    }
                    else -> {
                        displayToast("LISTENER different")
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        splitInstallManager = SplitInstallManagerFactory.create(this);
        splitInstallManager.registerListener(stateInstallListener)

        val installedModulesList = splitInstallManager.installedModules.toString()
        installedModules.text = installedModulesList.substring(1, installedModulesList.length - 1)

        sunflowerButton.setOnClickListener { installModuleOnDemand(SUNFLOWER) }
        daisyButton.setOnClickListener { installModuleOnDemand(DAISY) }
        tulipButton.setOnClickListener { installModuleOnDemand(TULIP) }
    }

    override fun onResume() {
        super.onResume()
        sunflowerButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkIfDownloaded(SUNFLOWER, splitInstallManager.installedModules),0)
        daisyButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkIfDownloaded(DAISY, splitInstallManager.installedModules),0)
        tulipButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkIfDownloaded(TULIP, splitInstallManager.installedModules),0)
    }

    private fun installModuleOnDemand(moduleName: String) {
        val request = SplitInstallRequest.newBuilder()
            .addModule(moduleName)
            .build()

        splitInstallManager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                mySessionId = sessionId
                displaySuccessToast(sessionId)
                launchModule(getFeaturePackage(moduleName))
            }
            .addOnFailureListener { exception -> displayFailureToast(exception) }
            .addOnCompleteListener { task -> displayCompleteToast(task) }
    }

    private fun launchModule(className: String) {
        val intent = Intent().setClassName(BuildConfig.APPLICATION_ID, className)
        startActivity(intent)
    }

    private fun displaySuccessToast(id: Int) {
        displayToast("Success $id")
    }

    private fun displayFailureToast(exception: Exception) {
        displayToast("Failure ${exception.message}")
    }

    private fun displayCompleteToast(task: Task<Int>) {
        displayToast("Complete ${task.isSuccessful}")
    }

    private fun displayToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun getFeaturePackage(feature: String): String = when(feature) {
        SUNFLOWER -> sunflowerPackage
        DAISY -> daisyPackage
        TULIP -> tulipPackage
        else -> ""
    }

    private fun checkIfDownloaded(feature: String, features: Set<String>): Int =
        if(features.contains(feature)) {
            R.drawable.ic_check
        } else {
            R.drawable.ic_cross_24
        }


    private companion object {
        const val SUNFLOWER = "sunflower"
        const val DAISY = "daisy"
        const val TULIP = "tulip"

        const val sunflowerPackage = "com.example.sunflower.SunflowerActivity"
        const val daisyPackage = "com.example.daisy.DaisyActivity"
        const val tulipPackage = "com.example.tulip.TulipActivity"
    }

}