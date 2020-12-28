package io.github.videosplitterapp.screens.license

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LicenseActivityBinding

@AndroidEntryPoint
class ToSActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val item = LicenseItem(
            name = "Terms of service",
            text = "\n" +
                    "Video Splitter App Terms of Service\n" +
                    "1. Terms\n" +
                    "By accessing our app, Video Splitter, you are agreeing to be bound by these terms of service, all applicable laws and regulations, and agree that you are responsible for compliance with any applicable local laws. If you do not agree with any of these terms, you are prohibited from using or accessing Video Splitter. The materials contained in Video Splitter are protected by applicable copyright and trademark law.\n" +
                    "2. Use License\n" +
                    "\n" +
                    "\n" +
                    "Permission is granted to temporarily download one copy of Video Splitter per device for personal, non-commercial transitory viewing only. This is the grant of a license, not a transfer of title, and under this license you may not:\n" +
                    "\n" +
                    "modify or copy the materials;\n" +
                    "use the materials for any commercial purpose, or for any public display (commercial or non-commercial);\n" +
                    "attempt to decompile or reverse engineer any software contained in Video Splitter;\n" +
                    "remove any copyright or other proprietary notations from the materials; or\n" +
                    "transfer the materials to another person or \"mirror\" the materials on any other server.\n" +
                    "\n" +
                    "\n" +
                    "This license shall automatically terminate if you violate any of these restrictions and may be terminated by Video Splitter App at any time. Upon terminating your viewing of these materials or upon the termination of this license, you must destroy any downloaded materials in your possession whether in electronic or printed format.\n" +
                    "\n" +
                    "3. Disclaimer\n" +
                    "\n" +
                    "The materials within Video Splitter are provided on an 'as is' basis. Video Splitter App makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties including, without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights.\n" +
                    "Further, Video Splitter App does not warrant or make any representations concerning the accuracy, likely results, or reliability of the use of the materials on its website or otherwise relating to such materials or on any sites linked to Video Splitter.\n" +
                    "\n" +
                    "4. Limitations\n" +
                    "In no event shall Video Splitter App or its suppliers be liable for any damages (including, without limitation, damages for loss of data or profit, or due to business interruption) arising out of the use or inability to use Video Splitter, even if Video Splitter App or a Video Splitter App authorized representative has been notified orally or in writing of the possibility of such damage. Because some jurisdictions do not allow limitations on implied warranties, or limitations of liability for consequential or incidental damages, these limitations may not apply to you.\n" +
                    "5. Accuracy of materials\n" +
                    "The materials appearing in Video Splitter could include technical, typographical, or photographic errors. Video Splitter App does not warrant that any of the materials on Video Splitter are accurate, complete or current. Video Splitter App may make changes to the materials contained in Video Splitter at any time without notice. However Video Splitter App does not make any commitment to update the materials.\n" +
                    "6. Links\n" +
                    "Video Splitter App has not reviewed all of the sites linked to its app and is not responsible for the contents of any such linked site. The inclusion of any link does not imply endorsement by Video Splitter App of the site. Use of any such linked website is at the user's own risk.\n" +
                    "7. Modifications\n" +
                    "Video Splitter App may revise these terms of service for its app at any time without notice. By using Video Splitter you are agreeing to be bound by the then current version of these terms of service.\n" +
                    "8. Governing Law\n" +
                    "These terms and conditions are governed by and construed in accordance with the laws of Maharashtra, India and you irrevocably submit to the exclusive jurisdiction of the courts in that State or location.\n" +
                    "Terms of Use created with GetTerms.\n"
        )
        DataBindingUtil.setContentView<LicenseActivityBinding>(this, R.layout.license_activity)
            .also {
                it.item = item
                it.lifecycleOwner = this
                setSupportActionBar(it.topAppBar)
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    title = item.name
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}