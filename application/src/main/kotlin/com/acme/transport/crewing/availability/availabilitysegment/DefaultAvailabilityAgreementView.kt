package com.acme.transport.crewing.availability.availabilitysegment

/** How screens and exports show a default availability agreement. */
data class DefaultAvailabilityAgreementView(
    var codeText: String,
    var labelText: String,
    var score: String,
    var pinnedText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toDefaultAvailabilityAgreementView(item: DefaultAvailabilityAgreement): DefaultAvailabilityAgreementView {
    return DefaultAvailabilityAgreementView(
        codeText = item.code,
        labelText = item.label,
        score = "%.2f".format(item.score),
        pinnedText = "${item.pinned}",
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAvailabilityAgreementViews(items: MutableList<DefaultAvailabilityAgreement>): MutableList<DefaultAvailabilityAgreementView> {
    val views = mutableListOf<DefaultAvailabilityAgreementView>()
    for (item in items) {
        views.add(toDefaultAvailabilityAgreementView(item))
    }
    return views
}

fun sumDefaultAvailabilityAgreementView(view: DefaultAvailabilityAgreementView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
