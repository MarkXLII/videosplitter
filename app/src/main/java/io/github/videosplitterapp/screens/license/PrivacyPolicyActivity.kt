package io.github.videosplitterapp.screens.license

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import io.github.videosplitterapp.R
import io.github.videosplitterapp.databinding.LicenseActivityBinding

@AndroidEntryPoint
class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val item = LicenseItem(
            name = "Privacy Policy",
            text = "\n" +
                    "Privacy Policy\n" +
                    "Your privacy is important to us. It is Video Splitter App's policy to respect your privacy regarding any information we may collect from you through our app, Video Splitter.\n" +
                    "We only ask for personal information when we truly need it to provide a service to you. We collect it by fair and lawful means, with your knowledge and consent. We also let you know why we’re collecting it and how it will be used.\n" +
                    "We only retain collected information for as long as necessary to provide you with your requested service. What data we store, we’ll protect within commercially acceptable means to prevent loss and theft, as well as unauthorized access, disclosure, copying, use or modification.\n" +
                    "We don’t share any personally identifying information publicly or with third-parties, except when required to by law.\n" +
                    "Our app may link to external sites that are not operated by us. Please be aware that we have no control over the content and practices of these sites, and cannot accept responsibility or liability for their respective privacy policies.\n" +
                    "You are free to refuse our request for your personal information, with the understanding that we may be unable to provide you with some of your desired services.\n" +
                    "Your continued use of our app will be regarded as acceptance of our practices around privacy and personal information. If you have any questions about how we handle user data and personal information, feel free to contact us.\n" +
                    "This policy is effective as of 12 July 2020.\n" +
                    "Privacy Policy created with GetTerms. "
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