package com.acme.transport.fleetops.utilization.milestone

/** How screens and exports show a rolling utilization quota. */
data class RollingUtilizationQuotaView(
    var code: String,
    var name: String,
    var channel: String,
    var limitText: String,
    var weight: String,
    var fuelLitersText: String,
    var visible: String,
    var statusLabel: String,
)

fun toRollingUtilizationQuotaView(item: RollingUtilizationQuota): RollingUtilizationQuotaView {
    return RollingUtilizationQuotaView(
        code = item.code,
        name = item.name,
        channel = item.channel,
        limitText = item.limit.toString(),
        weight = "%.2f".format(item.weight),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingUtilizationQuotaViews(items: MutableList<RollingUtilizationQuota>): MutableList<RollingUtilizationQuotaView> {
    val views = mutableListOf<RollingUtilizationQuotaView>()
    for (item in items) {
        views.add(toRollingUtilizationQuotaView(item))
    }
    return views
}

fun sumRollingUtilizationQuotaView(view: RollingUtilizationQuotaView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedLimit.toDouble() + parsedWeight + parsedFuelLiters
}
