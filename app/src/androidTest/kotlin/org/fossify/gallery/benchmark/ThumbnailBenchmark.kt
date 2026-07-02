package org.fossify.gallery.benchmark

import android.graphics.BitmapFactory
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThumbnailBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun decodeFullResolution() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        benchmarkRule.measureRepeated {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
            }
            BitmapFactory.decodeResource(context.resources, android.R.drawable.sym_def_app_icon, options)
        }
    }

    @Test
    fun decodeOverriddenSize() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        benchmarkRule.measureRepeated {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 4
            }
            BitmapFactory.decodeResource(context.resources, android.R.drawable.sym_def_app_icon, options)
        }
    }
}
