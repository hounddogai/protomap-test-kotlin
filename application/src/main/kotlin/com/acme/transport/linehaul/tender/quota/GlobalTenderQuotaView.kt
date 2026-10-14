package com.acme.transport.linehaul.tender.quota

/** How screens and exports show a global tender quota. */
data class GlobalTenderQuotaView(
    var code: String,
    var labelText: String,
    var descriptionText: String,
    var reviewer: String,
    var carrierText: String,
    var floorValue: String,
    var ceiling: String,
    var visible: String,
    var attempts: String,
    var statusLabel: String,
)

fun toGlobalTenderQuotaView(item: GlobalTenderQuota): GlobalTenderQuotaView {
    return GlobalTenderQuotaView(
        code = item.code,
        labelText = item.label,
        descriptionText = item.description,
        reviewer = item.reviewer,
        carrierText = item.carrier,
        floorValue = "%.2f".format(item.floorValue),
        ceiling = "%.2f".format(item.ceiling),
        visible = "${item.visible}",
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderQuotaViews(items: MutableList<GlobalTenderQuota>): MutableList<GlobalTenderQuotaView> {
    val views = mutableListOf<GlobalTenderQuotaView>()
    for (item in items) {
        views.add(toGlobalTenderQuotaView(item))
    }
    return views
}

fun sumGlobalTenderQuotaView(view: GlobalTenderQuotaView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedFloorValue + parsedCeiling + parsedAttempts.toDouble()
}
