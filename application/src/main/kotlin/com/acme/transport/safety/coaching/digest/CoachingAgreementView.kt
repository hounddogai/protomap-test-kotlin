package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching agreement. */
data class CoachingAgreementView(
    var code: String,
    var name: String,
    var vehicleId: String,
    var endsAtText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toCoachingAgreementView(item: CoachingAgreement): CoachingAgreementView {
    return CoachingAgreementView(
        code = item.code,
        name = truncate(item.name, 64),
        vehicleId = item.vehicleId,
        endsAtText = item.endsAt.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingAgreementViews(items: MutableList<CoachingAgreement>): MutableList<CoachingAgreementView> {
    val views = mutableListOf<CoachingAgreementView>()
    for (item in items) {
        views.add(toCoachingAgreementView(item))
    }
    return views
}

fun sumCoachingAgreementView(view: CoachingAgreementView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedEndsAt.toDouble() + parsedThreshold
}
