package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional hub snapshot. */
data class RegionalHubSnapshotView(
    var codeText: String,
    var name: String,
    var pallets: String,
    var visible: String,
    var budget: String,
    var statusLabel: String,
)

fun toRegionalHubSnapshotView(item: RegionalHubSnapshot): RegionalHubSnapshotView {
    return RegionalHubSnapshotView(
        codeText = truncate(item.code, 64),
        name = truncate(item.name, 64),
        pallets = item.pallets.toString(),
        visible = "${item.visible}",
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalHubSnapshotViews(items: MutableList<RegionalHubSnapshot>): MutableList<RegionalHubSnapshotView> {
    val views = mutableListOf<RegionalHubSnapshotView>()
    for (item in items) {
        views.add(toRegionalHubSnapshotView(item))
    }
    return views
}

fun sumRegionalHubSnapshotView(view: RegionalHubSnapshotView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedPallets.toDouble() + parsedBudget
}
