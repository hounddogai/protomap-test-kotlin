package com.acme.transport.fleetops.serviceplan.recorditem

/** How screens and exports show a rolling service plan tier. */
data class RollingServicePlanTierView(
    var code: String,
    var label: String,
    var minimum: String,
    var updatedAt: String,
    var weight: String,
    var loadKgText: String,
    var version: String,
    var title: String,
    var attempts: String,
    var statusLabel: String,
)

fun toRollingServicePlanTierView(item: RollingServicePlanTier): RollingServicePlanTierView {
    return RollingServicePlanTierView(
        code = item.code,
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        updatedAt = item.updatedAt.toString(),
        weight = "%.2f".format(item.weight),
        loadKgText = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        title = item.title,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingServicePlanTierViews(items: MutableList<RollingServicePlanTier>): MutableList<RollingServicePlanTierView> {
    val views = mutableListOf<RollingServicePlanTierView>()
    for (item in items) {
        views.add(toRollingServicePlanTierView(item))
    }
    return views
}

fun sumRollingServicePlanTierView(view: RollingServicePlanTierView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedMinimum + parsedUpdatedAt.toDouble() + parsedWeight
}
