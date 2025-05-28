package com.reinsteinquizbowl.order.adapter

import com.reinsteinquizbowl.order.util.Config
import com.reinsteinquizbowl.order.util.EmailAddress
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.springframework.stereotype.Service

@Service
@Suppress("TooGenericExceptionThrown") // what else am I supposed to do for external services failing?
class EmailAdapter {
    @Suppress("TooGenericExceptionCaught")
    fun sendHtmlEmail(
        from: EmailAddress,
        to: EmailAddress,
        ccs: List<EmailAddress> = emptyList(),
        subject: String,
        bodyHtml: String,
    ) {
        val email = HtmlEmail()
        email.hostName = Config.SMTP_SERVER
        email.setSmtpPort(Config.SMTP_PORT)
        email.authenticator = DefaultAuthenticator(Config.SMTP_USERNAME, Config.SMTP_PASSWORD)
        email.setSSLOnConnect(true)
        email.setFrom(from.address, from.description)
        email.addTo(to.address, to.description)
        email.subject = subject
        email.setHtmlMsg(bodyHtml)

        for (cc in ccs) {
            email.addCc(cc.address, cc.description)
        }

        email.send()
    }
}
