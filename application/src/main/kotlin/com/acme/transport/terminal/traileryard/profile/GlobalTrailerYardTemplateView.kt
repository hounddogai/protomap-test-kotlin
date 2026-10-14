package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global trailer yard template. */
data class GlobalTrailerYardTemplateView(
    var codeText: String,
    var titleText: String,
    var etaMinutesText: String,
    var retries: String,
    var revision: String,
    var attempts: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toGlobalTrailerYardTemplateView(item: GlobalTrailerYardTemplate): GlobalTrailerYardTemplateView {
    return GlobalTrailerYardTemplateView(
        codeText = item.code,
        titleText = item.title,
        etaMinutesText = item.etaMinutes.toString(),
        retries = item.retries.toString(),
        revision = item.revision.toString(),
        attempts = item.attempts.toString(),
        contactEmail = truncate(item.contactEmail, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTrailerYardTemplateViews(items: MutableList<GlobalTrailerYardTemplate>): MutableList<GlobalTrailerYardTemplateView> {
    val views = mutableListOf<GlobalTrailerYardTemplateView>()
    for (item in items) {
        views.add(toGlobalTrailerYardTemplateView(item))
    }
    return views
}

fun sumGlobalTrailerYardTemplateView(view: GlobalTrailerYardTemplateView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedEtaMinutes.toDouble() + parsedRetries.toDouble() + parsedRevision.toDouble()
}
