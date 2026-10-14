package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling milestone. */
data class IdlingMilestoneView(
    var code: String,
    var titleText: String,
    var maximum: String,
    var category: String,
    var etaMinutes: String,
    var checksumText: String,
    var weight: String,
    var contactEmail: String,
    var driverId: String,
    var statusLabel: String,
)

fun toIdlingMilestoneView(item: IdlingMilestone): IdlingMilestoneView {
    return IdlingMilestoneView(
        code = item.code,
        titleText = item.title,
        maximum = "%.2f".format(item.maximum),
        category = truncate(item.category, 40),
        etaMinutes = item.etaMinutes.toString(),
        checksumText = item.checksum,
        weight = "%.2f".format(item.weight),
        contactEmail = truncate(item.contactEmail, 40),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingMilestoneViews(items: MutableList<IdlingMilestone>): MutableList<IdlingMilestoneView> {
    val views = mutableListOf<IdlingMilestoneView>()
    for (item in items) {
        views.add(toIdlingMilestoneView(item))
    }
    return views
}

fun sumIdlingMilestoneView(view: IdlingMilestoneView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedMaximum + parsedEtaMinutes.toDouble() + parsedWeight
}
