package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service plan rollup. */
data class FallbackServicePlanRollupView(
    var codeText: String,
    var labelText: String,
    var routeCode: String,
    var pallets: String,
    var dockText: String,
    var updatedAt: String,
    var maximumText: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toFallbackServicePlanRollupView(item: FallbackServicePlanRollup): FallbackServicePlanRollupView {
    return FallbackServicePlanRollupView(
        codeText = item.code,
        labelText = item.label,
        routeCode = item.routeCode,
        pallets = item.pallets.toString(),
        dockText = truncate(item.dock, 120),
        updatedAt = item.updatedAt.toString(),
        maximumText = "%.2f".format(item.maximum),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServicePlanRollupViews(items: MutableList<FallbackServicePlanRollup>): MutableList<FallbackServicePlanRollupView> {
    val views = mutableListOf<FallbackServicePlanRollupView>()
    for (item in items) {
        views.add(toFallbackServicePlanRollupView(item))
    }
    return views
}

fun sumFallbackServicePlanRollupView(view: FallbackServicePlanRollupView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedPallets.toDouble() + parsedUpdatedAt.toDouble() + parsedMaximum
}
