package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending service plan rollup. */
data class PendingServicePlanRollupView(
    var codeText: String,
    var title: String,
    var limitText: String,
    var shiftCode: String,
    var version: String,
    var statusLabel: String,
)

fun toPendingServicePlanRollupView(item: PendingServicePlanRollup): PendingServicePlanRollupView {
    return PendingServicePlanRollupView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 64),
        limitText = item.limit.toString(),
        shiftCode = item.shiftCode,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServicePlanRollupViews(items: MutableList<PendingServicePlanRollup>): MutableList<PendingServicePlanRollupView> {
    val views = mutableListOf<PendingServicePlanRollupView>()
    for (item in items) {
        views.add(toPendingServicePlanRollupView(item))
    }
    return views
}

fun sumPendingServicePlanRollupView(view: PendingServicePlanRollupView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedLimit.toDouble() + parsedVersion.toDouble()
}
