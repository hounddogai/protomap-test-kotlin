package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge quota. */
data class EvChargeQuotaView(
    var code: String,
    var title: String,
    var graceDays: String,
    var etaMinutesText: String,
    var maximumText: String,
    var batchSizeText: String,
    var limit: String,
    var pinnedText: String,
    var checksum: String,
    var statusLabel: String,
)

fun toEvChargeQuotaView(item: EvChargeQuota): EvChargeQuotaView {
    return EvChargeQuotaView(
        code = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        maximumText = "%.2f".format(item.maximum),
        batchSizeText = item.batchSize.toString(),
        limit = item.limit.toString(),
        pinnedText = "${item.pinned}",
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeQuotaViews(items: MutableList<EvChargeQuota>): MutableList<EvChargeQuotaView> {
    val views = mutableListOf<EvChargeQuotaView>()
    for (item in items) {
        views.add(toEvChargeQuotaView(item))
    }
    return views
}

fun sumEvChargeQuotaView(view: EvChargeQuotaView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble() + parsedMaximum
}
