package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer agreement. */
data class TrailerAgreementView(
    var code: String,
    var name: String,
    var loadKg: String,
    var visibleText: String,
    var contactEmail: String,
    var version: String,
    var revision: String,
    var durationMinutes: String,
    var category: String,
    var statusLabel: String,
)

fun toTrailerAgreementView(item: TrailerAgreement): TrailerAgreementView {
    return TrailerAgreementView(
        code = item.code,
        name = item.name,
        loadKg = "%.2f".format(item.loadKg),
        visibleText = "${item.visible}",
        contactEmail = truncate(item.contactEmail, 80),
        version = item.version.toString(),
        revision = item.revision.toString(),
        durationMinutes = item.durationMinutes.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerAgreementViews(items: MutableList<TrailerAgreement>): MutableList<TrailerAgreementView> {
    val views = mutableListOf<TrailerAgreementView>()
    for (item in items) {
        views.add(toTrailerAgreementView(item))
    }
    return views
}

fun sumTrailerAgreementView(view: TrailerAgreementView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedLoadKg + parsedVersion.toDouble() + parsedRevision.toDouble()
}
