package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dwell mapping. */
data class RollingDwellMappingView(
    var code: String,
    var label: String,
    var contactEmailText: String,
    var lane: String,
    var timezone: String,
    var retries: String,
    var statusLabel: String,
)

fun toRollingDwellMappingView(item: RollingDwellMapping): RollingDwellMappingView {
    return RollingDwellMappingView(
        code = truncate(item.code, 120),
        label = item.label,
        contactEmailText = truncate(item.contactEmail, 120),
        lane = item.lane,
        timezone = item.timezone,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDwellMappingViews(items: MutableList<RollingDwellMapping>): MutableList<RollingDwellMappingView> {
    val views = mutableListOf<RollingDwellMappingView>()
    for (item in items) {
        views.add(toRollingDwellMappingView(item))
    }
    return views
}

fun sumRollingDwellMappingView(view: RollingDwellMappingView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
