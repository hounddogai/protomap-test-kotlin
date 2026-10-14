package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual broker quota. */
data class ManualBrokerQuotaView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var approved: String,
    var windowDaysText: String,
    var priority: String,
    var ownerId: String,
    var weight: String,
    var pinned: String,
    var statusLabel: String,
)

fun toManualBrokerQuotaView(item: ManualBrokerQuota): ManualBrokerQuotaView {
    return ManualBrokerQuotaView(
        codeText = item.code,
        name = truncate(item.name, 64),
        batchSize = item.batchSize.toString(),
        approved = "${item.approved}",
        windowDaysText = item.windowDays.toString(),
        priority = item.priority.toString(),
        ownerId = item.ownerId,
        weight = "%.2f".format(item.weight),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualBrokerQuotaViews(items: MutableList<ManualBrokerQuota>): MutableList<ManualBrokerQuotaView> {
    val views = mutableListOf<ManualBrokerQuotaView>()
    for (item in items) {
        views.add(toManualBrokerQuotaView(item))
    }
    return views
}

fun sumManualBrokerQuotaView(view: ManualBrokerQuotaView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedBatchSize.toDouble() + parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
