package com.gauravtak.fingerPrintAuthentication

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


fun checkBiometricSupport(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            println("No biometric hardware available")
            false
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            println("Biometric hardware currently unavailable")
            false
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            println("No fingerprint enrolled")
            false
        }
        else -> false
    }
}


fun showBiometricPrompt(activity: FragmentActivity) {
    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                println("Auth error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                println("Authentication succeeded!")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                println("Authentication failed")
            }
        })

    val promptInfo = PromptInfo.Builder()
        .setTitle("Unlock App")
        .setSubtitle("Authenticate using your fingerprint")
        .setNegativeButtonText("Use Password")
        .build()

    biometricPrompt.authenticate(promptInfo)
}


fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onFallback: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                println("Authentication error: $errString")
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                     onFallback()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                println("Authentication succeeded!")
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                println("Authentication failed.")
            }
        })

    val promptInfo: PromptInfo
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
        // Use both strong biometrics and device credential
        promptInfo = PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Please use your fingerprint or device PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    } else { // API 29 and lower
        // Use only strong biometrics (or device credential if preferred)
        promptInfo = PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Please use your fingerprint")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Use PIN Instead") // A negative button is required if DEVICE_CREDENTIAL is not allowed
            .build()
    }
    biometricPrompt.authenticate(promptInfo)
}
