package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional cutoff target. */
data class RegionalCutoffTargetView(
    var code: String,
    var nameText: String,
    var unitText: String,
    var ownerId: String,
    var title: String,
    var activeText: String,
    var timezone: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toRegionalCutoffTargetView(item: RegionalCutoffTarget): RegionalCutoffTargetView {
    return RegionalCutoffTargetView(
        code = item.code,
        nameText = truncate(item.name, 64),
        unitText = truncate(item.unit, 64),
        ownerId = item.ownerId,
        title = item.title,
        activeText = "${item.active}",
        timezone = item.timezone,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCutoffTargetViews(items: MutableList<RegionalCutoffTarget>): MutableList<RegionalCutoffTargetView> {
    val views = mutableListOf<RegionalCutoffTargetView>()
    for (item in items) {
        views.add(toRegionalCutoffTargetView(item))
    }
    return views
}

fun sumRegionalCutoffTargetView(view: RegionalCutoffTargetView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedCreatedAt.toDouble()
}
