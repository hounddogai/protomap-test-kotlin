package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity score. */
data class CapacityScoreView(
    var code: String,
    var label: String,
    var segmentText: String,
    var etaMinutes: String,
    var ownerId: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toCapacityScoreView(item: CapacityScore): CapacityScoreView {
    return CapacityScoreView(
        code = item.code,
        label = truncate(item.label, 40),
        segmentText = item.segment,
        etaMinutes = item.etaMinutes.toString(),
        ownerId = item.ownerId,
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityScoreViews(items: MutableList<CapacityScore>): MutableList<CapacityScoreView> {
    val views = mutableListOf<CapacityScoreView>()
    for (item in items) {
        views.add(toCapacityScoreView(item))
    }
    return views
}

fun sumCapacityScoreView(view: CapacityScoreView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble()
}
