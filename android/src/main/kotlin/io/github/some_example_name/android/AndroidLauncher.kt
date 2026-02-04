package io.github.some_example_name.android

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import io.github.some_example_name.main

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(main(), AndroidApplicationConfiguration().apply {
            // Configure your application here.
            useImmersiveMode = true // Recommended, but not required.
        })
    }
}
