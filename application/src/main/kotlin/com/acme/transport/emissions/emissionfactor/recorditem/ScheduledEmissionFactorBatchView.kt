package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled emission factor batch. */
data class ScheduledEmissionFactorBatchView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var locale: String,
    var version: String,
    var etaMinutes: String,
    var stopCount: String,
    var expiresAt: String,
    var contactEmail: String,
    var lane: String,
    var statusLabel: String,
)

fun toScheduledEmissionFactorBatchView(item: ScheduledEmissionFactorBatch): ScheduledEmissionFactorBatchView {
    return ScheduledEmissionFactorBatchView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 64),
        thresholdText = "%.2f".format(item.threshold),
        locale = item.locale,
        version = item.version.toString(),
        etaMinutes = item.etaMinutes.toString(),
        stopCount = item.stopCount.toString(),
        expiresAt = item.expiresAt.toString(),
        contactEmail = item.contactEmail,
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledEmissionFactorBatchViews(items: MutableList<ScheduledEmissionFactorBatch>): MutableList<ScheduledEmissionFactorBatchView> {
    val views = mutableListOf<ScheduledEmissionFactorBatchView>()
    for (item in items) {
        views.add(toScheduledEmissionFactorBatchView(item))
    }
    return views
}

fun sumScheduledEmissionFactorBatchView(view: ScheduledEmissionFactorBatchView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedThreshold + parsedVersion.toDouble() + parsedEtaMinutes.toDouble()
}
