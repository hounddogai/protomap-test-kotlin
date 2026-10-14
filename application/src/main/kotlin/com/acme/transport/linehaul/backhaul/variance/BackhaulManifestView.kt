package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul manifest. */
data class BackhaulManifestView(
    var codeText: String,
    var label: String,
    var checksum: String,
    var loadKg: String,
    var pallets: String,
    var cost: String,
    var externalRef: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toBackhaulManifestView(item: BackhaulManifest): BackhaulManifestView {
    return BackhaulManifestView(
        codeText = item.code,
        label = item.label,
        checksum = item.checksum,
        loadKg = "%.2f".format(item.loadKg),
        pallets = item.pallets.toString(),
        cost = "%.2f".format(item.cost),
        externalRef = truncate(item.externalRef, 40),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulManifestViews(items: MutableList<BackhaulManifest>): MutableList<BackhaulManifestView> {
    val views = mutableListOf<BackhaulManifestView>()
    for (item in items) {
        views.add(toBackhaulManifestView(item))
    }
    return views
}

fun sumBackhaulManifestView(view: BackhaulManifestView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedLoadKg + parsedPallets.toDouble() + parsedCost
}
