package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled proof of delivery entry. */
data class ScheduledProofOfDeliveryEntryView(
    var code: String,
    var name: String,
    var maximumText: String,
    var attemptsText: String,
    var title: String,
    var statusLabel: String,
)

fun toScheduledProofOfDeliveryEntryView(item: ScheduledProofOfDeliveryEntry): ScheduledProofOfDeliveryEntryView {
    return ScheduledProofOfDeliveryEntryView(
        code = truncate(item.code, 120),
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        attemptsText = item.attempts.toString(),
        title = truncate(item.title, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledProofOfDeliveryEntryViews(items: MutableList<ScheduledProofOfDeliveryEntry>): MutableList<ScheduledProofOfDeliveryEntryView> {
    val views = mutableListOf<ScheduledProofOfDeliveryEntryView>()
    for (item in items) {
        views.add(toScheduledProofOfDeliveryEntryView(item))
    }
    return views
}

fun sumScheduledProofOfDeliveryEntryView(view: ScheduledProofOfDeliveryEntryView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedMaximum + parsedAttempts.toDouble()
}
