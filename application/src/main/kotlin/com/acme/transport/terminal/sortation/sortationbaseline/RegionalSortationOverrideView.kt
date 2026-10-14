package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional sortation override. */
data class RegionalSortationOverrideView(
    var code: String,
    var name: String,
    var externalRef: String,
    var contactEmail: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toRegionalSortationOverrideView(item: RegionalSortationOverride): RegionalSortationOverrideView {
    return RegionalSortationOverrideView(
        code = truncate(item.code, 64),
        name = item.name,
        externalRef = truncate(item.externalRef, 64),
        contactEmail = item.contactEmail,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalSortationOverrideViews(items: MutableList<RegionalSortationOverride>): MutableList<RegionalSortationOverrideView> {
    val views = mutableListOf<RegionalSortationOverrideView>()
    for (item in items) {
        views.add(toRegionalSortationOverrideView(item))
    }
    return views
}

fun sumRegionalSortationOverrideView(view: RegionalSortationOverrideView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
