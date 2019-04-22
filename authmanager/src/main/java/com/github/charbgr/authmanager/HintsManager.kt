package com.github.charbgr.authmanager

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import com.github.charbgr.authmanager.views.HintView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import java.lang.ref.WeakReference


class HintsManager private constructor(builder: HintsManagerBuilder) {

    companion object {

        @JvmStatic
        fun Builder(activity: AppCompatActivity?): HintsManagerBuilder
            = HintsManagerBuilder(activity)

        @JvmStatic
        fun withBuilder(hintsManagerBuilder: HintsManagerBuilder): HintsManager {
            return HintsManager(hintsManagerBuilder)
        }
    }

    private var activity: AppCompatActivity?

    private var googleApiClient: GoogleApiClient? = null

    private var hintView: WeakReference<HintView>
    private var hintRequest: HintRequest? = null

    init {
        activity = builder.activity

        googleApiClient = builder.googleApiClient

        hintView = WeakReference(builder.hintView)
        hintRequest = builder.hintRequest
    }

    /**
     * Request Email Hints
     * */
    fun requestEmailHints() {
        val intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
        try {
            activity?.startIntentSenderForResult(intent.intentSender, AuthManagerCodes.RC_HINT_REQUEST, null, 0, 0, 0)
        } catch (e: IntentSender.SendIntentException) {
            hintView.get()?.emailHintRequestFailure()
        }
    }

    fun handle(resultCode: Int, data: Intent?) {
        handleEmailHintRequestResolution(resultCode, data)
    }

    fun clear() {
        hintView.clear()
    }

    /**
     * Handle Hint Request Resolution
     * */
    private fun handleEmailHintRequestResolution(resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            hintView.get()?.emailHintRequestCancelled()
        } else {
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.let {
                hintView.get()?.emailHintSelected(it)
            }
        }
    }

    class HintsManagerBuilder internal constructor(val activity: AppCompatActivity?) {

        lateinit var hintView: HintView
            private set

        lateinit var hintRequest: HintRequest
            private set

        lateinit var googleApiClient: GoogleApiClient
            private set

        fun withGoogleApiClient(googleApiClient: GoogleApiClient) = apply {
            this.googleApiClient = googleApiClient
        }

        fun withHintsView(hintView: HintView) = apply {
            this.hintView = hintView
        }

        fun withHintRequest(hintRequest: HintRequest) = apply {
            this.hintRequest = hintRequest
        }

        fun build(): HintsManager = Companion.withBuilder(this)
    }

}


