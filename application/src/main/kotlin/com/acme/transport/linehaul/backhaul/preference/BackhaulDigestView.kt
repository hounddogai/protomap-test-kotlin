package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul digest. */
data class BackhaulDigestView(
    var code: String,
    var name: String,
    var cost: String,
    var revision: String,
    var loadKgText: String,
    var version: String,
    var currency: String,
    var statusLabel: String,
)

fun toBackhaulDigestView(item: BackhaulDigest): BackhaulDigestView {
    return BackhaulDigestView(
        code = item.code,
        name = truncate(item.name, 120),
        cost = "%.2f".format(item.cost),
        revision = item.revision.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        currency = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulDigestViews(items: MutableList<BackhaulDigest>): MutableList<BackhaulDigestView> {
    val views = mutableListOf<BackhaulDigestView>()
    for (item in items) {
        views.add(toBackhaulDigestView(item))
    }
    return views
}

fun sumBackhaulDigestView(view: BackhaulDigestView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedCost + parsedRevision.toDouble() + parsedLoadKg
}
