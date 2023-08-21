package com.reinsteinquizbowl.order.adapter

import com.reinsteinquizbowl.order.util.Config
import com.reinsteinquizbowl.order.util.EmailAddress
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import org.springframework.stereotype.Service

@Service
@Suppress("TooGenericExceptionThrown") // what else am I supposed to do for external services failing?
class SendgridAdapter {
    private fun EmailAddress.toSendgridEmail() = Email(this.address, this.description)

    @Suppress("TooGenericExceptionCaught")
    fun sendHtmlEmail(
        from: EmailAddress,
        to: EmailAddress,
        cc: EmailAddress? = null,
        subject: String,
        bodyHtml: String,
    ) {
        val mail = Mail(from.toSendgridEmail(), subject, to.toSendgridEmail(), Content("text/html", bodyHtml))

        if (cc != null) {
            val personalization = Personalization()
            personalization.addTo(to.toSendgridEmail())
            personalization.addCc(cc.toSendgridEmail())
            mail.addPersonalization(personalization)
        }

        val sg = SendGrid(Config.SENDGRID_API_KEY)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sg.api(request)
            if (!GOOD_RESPONSE_CODES.contains(response.statusCode)) {
                throw RuntimeException("Unexpected response: code ${response.statusCode} with body ${response.body}")
            }
        } catch (ex: Exception) {
            throw RuntimeException("Couldn't send email to $to with subject $subject: ${ex.message}", ex)
        }
    }

    companion object {
        private val GOOD_RESPONSE_CODES = setOf(200, 201, 202, 204) // It seems to actually be 202 in practice for the only type of call we currently make. The others come from https://docs.sendgrid.com/api-reference/how-to-use-the-sendgrid-v3-api/responses , which, ironically, does not document 202.
    }
}
