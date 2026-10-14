package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual relay quota. */
data class ManualRelayQuotaView(
    var code: String,
    var titleText: String,
    var loadKg: String,
    var stopCount: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toManualRelayQuotaView(item: ManualRelayQuota): ManualRelayQuotaView {
    return ManualRelayQuotaView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 120),
        loadKg = "%.2f".format(item.loadKg),
        stopCount = item.stopCount.toString(),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRelayQuotaViews(items: MutableList<ManualRelayQuota>): MutableList<ManualRelayQuotaView> {
    val views = mutableListOf<ManualRelayQuotaView>()
    for (item in items) {
        views.add(toManualRelayQuotaView(item))
    }
    return views
}

fun sumManualRelayQuotaView(view: ManualRelayQuotaView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedLoadKg + parsedStopCount.toDouble()
}
