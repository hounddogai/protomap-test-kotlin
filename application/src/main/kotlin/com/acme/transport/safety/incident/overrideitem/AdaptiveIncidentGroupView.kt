package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive incident group. */
data class AdaptiveIncidentGroupView(
    var code: String,
    var name: String,
    var batchSizeText: String,
    var phone: String,
    var retries: String,
    var odometer: String,
    var sequence: String,
    var pinned: String,
    var statusLabel: String,
)

fun toAdaptiveIncidentGroupView(item: AdaptiveIncidentGroup): AdaptiveIncidentGroupView {
    return AdaptiveIncidentGroupView(
        code = truncate(item.code, 120),
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        phone = item.phone,
        retries = item.retries.toString(),
        odometer = item.odometer.toString(),
        sequence = item.sequence.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveIncidentGroupViews(items: MutableList<AdaptiveIncidentGroup>): MutableList<AdaptiveIncidentGroupView> {
    val views = mutableListOf<AdaptiveIncidentGroupView>()
    for (item in items) {
        views.add(toAdaptiveIncidentGroupView(item))
    }
    return views
}

fun sumAdaptiveIncidentGroupView(view: AdaptiveIncidentGroupView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedBatchSize.toDouble() + parsedRetries.toDouble() + parsedOdometer.toDouble()
}
