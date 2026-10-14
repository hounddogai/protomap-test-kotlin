package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling capacity rollup. */
data class RollingCapacityRollupView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var windowDays: String,
    var externalRef: String,
    var approved: String,
    var label: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toRollingCapacityRollupView(item: RollingCapacityRollup): RollingCapacityRollupView {
    return RollingCapacityRollupView(
        code = truncate(item.code, 120),
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        windowDays = item.windowDays.toString(),
        externalRef = item.externalRef,
        approved = "${item.approved}",
        label = truncate(item.label, 64),
        phoneText = truncate(item.phone, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCapacityRollupViews(items: MutableList<RollingCapacityRollup>): MutableList<RollingCapacityRollupView> {
    val views = mutableListOf<RollingCapacityRollupView>()
    for (item in items) {
        views.add(toRollingCapacityRollupView(item))
    }
    return views
}

fun sumRollingCapacityRollupView(view: RollingCapacityRollupView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble()
}
