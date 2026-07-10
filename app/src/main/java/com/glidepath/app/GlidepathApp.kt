package com.glidepath.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. Hosts the Hilt object graph for the whole app. */
@HiltAndroidApp
class GlidepathApp : Application()
