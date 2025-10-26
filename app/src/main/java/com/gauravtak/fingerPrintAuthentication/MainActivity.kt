package com.gauravtak.fingerPrintAuthentication

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.gauravtak.fingerPrintAuthentication.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun launchSecureFlowAuthentication() {
        if (checkBiometricSupport(this)) {
            showBiometricPrompt(
                activity = this,
                onSuccess = {
                    Toast.makeText(this, "Authenticated!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFallback = {
                    Toast.makeText(this, "Authentication onFallback", Toast.LENGTH_SHORT).show()
                    openAuthenticateFlowForResult()
                }
            )
        } else {
            openAuthenticateFlowForResult()
        }
    }

    private fun initViews() {
        binding.btnStartAuthentication1.setOnClickListener {
            launchSecureFlowAuthentication()
        }
        binding.btnStartAuthentication2.setOnClickListener {
            openAuthenticateFlowForResult()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Device credential verified!", Toast.LENGTH_SHORT).show()
             } else {
                Toast.makeText(this, "Authentication cancelled or failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openAuthenticateFlowForResult() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val keyguardIntent = keyguardManager.createConfirmDeviceCredentialIntent(
            "Verify Your Identity to initiate secure flow within ${this.getString(R.string.app_name)} app",
            "Authenticate to continue"
        )

        if (keyguardIntent != null) {
            resultLauncherOfAuthenticateFlow.launch(keyguardIntent)
        }
    }

    var resultLauncherOfAuthenticateFlow = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Authentication verified!", Toast.LENGTH_SHORT).show()
            Log.i("resultLauncher", "doSomeOperations() result success callBack $data")
        } else {
            Toast.makeText(this, "Authentication cancelled or failed.", Toast.LENGTH_SHORT).show()
            Log.i("resultLauncher", "doSomeOperations() result failure callBack $data")
        }
    }
}