package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped capacity mapping. */
data class CappedCapacityMappingView(
    var codeText: String,
    var label: String,
    var approved: String,
    var reviewer: String,
    var currencyText: String,
    var trailerId: String,
    var sequence: String,
    var etaMinutes: String,
    var pinned: String,
    var active: String,
    var statusLabel: String,
)

fun toCappedCapacityMappingView(item: CappedCapacityMapping): CappedCapacityMappingView {
    return CappedCapacityMappingView(
        codeText = item.code,
        label = item.label,
        approved = "${item.approved}",
        reviewer = item.reviewer,
        currencyText = item.currency,
        trailerId = truncate(item.trailerId, 120),
        sequence = item.sequence.toString(),
        etaMinutes = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCapacityMappingViews(items: MutableList<CappedCapacityMapping>): MutableList<CappedCapacityMappingView> {
    val views = mutableListOf<CappedCapacityMappingView>()
    for (item in items) {
        views.add(toCappedCapacityMappingView(item))
    }
    return views
}

fun sumCappedCapacityMappingView(view: CappedCapacityMappingView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedSequence.toDouble() + parsedEtaMinutes.toDouble()
}
