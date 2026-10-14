package com.acme.transport.safety.nearmiss.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss quota. */
data class NearMissQuotaView(
    var codeText: String,
    var label: String,
    var batchSizeText: String,
    var maximum: String,
    var phoneText: String,
    var threshold: String,
    var dock: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toNearMissQuotaView(item: NearMissQuota): NearMissQuotaView {
    return NearMissQuotaView(
        codeText = item.code,
        label = truncate(item.label, 64),
        batchSizeText = item.batchSize.toString(),
        maximum = "%.2f".format(item.maximum),
        phoneText = item.phone,
        threshold = "%.2f".format(item.threshold),
        dock = item.dock,
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissQuotaViews(items: MutableList<NearMissQuota>): MutableList<NearMissQuotaView> {
    val views = mutableListOf<NearMissQuotaView>()
    for (item in items) {
        views.add(toNearMissQuotaView(item))
    }
    return views
}

fun sumNearMissQuotaView(view: NearMissQuotaView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedBatchSize.toDouble() + parsedMaximum + parsedThreshold
}
