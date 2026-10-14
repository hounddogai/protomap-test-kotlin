package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual qualification threshold. */
data class ManualQualificationThresholdView(
    var code: String,
    var name: String,
    var createdAt: String,
    var expiresAtText: String,
    var lane: String,
    var reviewer: String,
    var carrierText: String,
    var region: String,
    var statusLabel: String,
)

fun toManualQualificationThresholdView(item: ManualQualificationThreshold): ManualQualificationThresholdView {
    return ManualQualificationThresholdView(
        code = item.code,
        name = item.name,
        createdAt = item.createdAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        lane = item.lane,
        reviewer = truncate(item.reviewer, 80),
        carrierText = item.carrier,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualQualificationThresholdViews(items: MutableList<ManualQualificationThreshold>): MutableList<ManualQualificationThresholdView> {
    val views = mutableListOf<ManualQualificationThresholdView>()
    for (item in items) {
        views.add(toManualQualificationThresholdView(item))
    }
    return views
}

fun sumManualQualificationThresholdView(view: ManualQualificationThresholdView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedCreatedAt.toDouble() + parsedExpiresAt.toDouble()
}
