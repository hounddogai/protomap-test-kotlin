package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled near miss quota. */
data class ScheduledNearMissQuotaView(
    var codeText: String,
    var name: String,
    var routeCode: String,
    var budgetText: String,
    var regionText: String,
    var batchSizeText: String,
    var checksum: String,
    var fuelLitersText: String,
    var rate: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toScheduledNearMissQuotaView(item: ScheduledNearMissQuota): ScheduledNearMissQuotaView {
    return ScheduledNearMissQuotaView(
        codeText = item.code,
        name = truncate(item.name, 80),
        routeCode = item.routeCode,
        budgetText = "%.2f".format(item.budget),
        regionText = item.region,
        batchSizeText = item.batchSize.toString(),
        checksum = truncate(item.checksum, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        rate = "%.2f".format(item.rate),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNearMissQuotaViews(items: MutableList<ScheduledNearMissQuota>): MutableList<ScheduledNearMissQuotaView> {
    val views = mutableListOf<ScheduledNearMissQuotaView>()
    for (item in items) {
        views.add(toScheduledNearMissQuotaView(item))
    }
    return views
}

fun sumScheduledNearMissQuotaView(view: ScheduledNearMissQuotaView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedBudget + parsedBatchSize.toDouble() + parsedFuelLiters
}
