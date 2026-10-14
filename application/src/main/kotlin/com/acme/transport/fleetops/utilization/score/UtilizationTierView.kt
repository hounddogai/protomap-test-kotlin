package com.acme.transport.fleetops.utilization.score

/** How screens and exports show a utilization tier. */
data class UtilizationTierView(
    var codeText: String,
    var labelText: String,
    var timezone: String,
    var rate: String,
    var driverId: String,
    var statusLabel: String,
)

fun toUtilizationTierView(item: UtilizationTier): UtilizationTierView {
    return UtilizationTierView(
        codeText = item.code,
        labelText = item.label,
        timezone = item.timezone,
        rate = "%.2f".format(item.rate),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationTierViews(items: MutableList<UtilizationTier>): MutableList<UtilizationTierView> {
    val views = mutableListOf<UtilizationTierView>()
    for (item in items) {
        views.add(toUtilizationTierView(item))
    }
    return views
}

fun sumUtilizationTierView(view: UtilizationTierView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
