package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched backhaul digest. */
data class BatchedBackhaulDigestView(
    var code: String,
    var title: String,
    var endsAtText: String,
    var vehicleId: String,
    var reviewer: String,
    var fuelLiters: String,
    var weight: String,
    var pinnedText: String,
    var visible: String,
    var statusLabel: String,
)

fun toBatchedBackhaulDigestView(item: BatchedBackhaulDigest): BatchedBackhaulDigestView {
    return BatchedBackhaulDigestView(
        code = truncate(item.code, 120),
        title = item.title,
        endsAtText = item.endsAt.toString(),
        vehicleId = item.vehicleId,
        reviewer = truncate(item.reviewer, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        weight = "%.2f".format(item.weight),
        pinnedText = "${item.pinned}",
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedBackhaulDigestViews(items: MutableList<BatchedBackhaulDigest>): MutableList<BatchedBackhaulDigestView> {
    val views = mutableListOf<BatchedBackhaulDigestView>()
    for (item in items) {
        views.add(toBatchedBackhaulDigestView(item))
    }
    return views
}

fun sumBatchedBackhaulDigestView(view: BatchedBackhaulDigestView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedEndsAt.toDouble() + parsedFuelLiters + parsedWeight
}
