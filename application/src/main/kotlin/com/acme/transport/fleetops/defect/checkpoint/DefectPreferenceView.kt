package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect preference. */
data class DefectPreferenceView(
    var code: String,
    var title: String,
    var startsAt: String,
    var rate: String,
    var limit: String,
    var carrier: String,
    var depot: String,
    var contactEmail: String,
    var batchSize: String,
    var locale: String,
    var statusLabel: String,
)

fun toDefectPreferenceView(item: DefectPreference): DefectPreferenceView {
    return DefectPreferenceView(
        code = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        rate = "%.2f".format(item.rate),
        limit = item.limit.toString(),
        carrier = item.carrier,
        depot = item.depot,
        contactEmail = truncate(item.contactEmail, 80),
        batchSize = item.batchSize.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectPreferenceViews(items: MutableList<DefectPreference>): MutableList<DefectPreferenceView> {
    val views = mutableListOf<DefectPreferenceView>()
    for (item in items) {
        views.add(toDefectPreferenceView(item))
    }
    return views
}

fun sumDefectPreferenceView(view: DefectPreferenceView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedStartsAt.toDouble() + parsedRate + parsedLimit.toDouble()
}
