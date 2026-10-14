package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker tier. */
data class BrokerTierView(
    var codeText: String,
    var nameText: String,
    var notes: String,
    var ratioText: String,
    var version: String,
    var externalRefText: String,
    var dock: String,
    var statusLabel: String,
)

fun toBrokerTierView(item: BrokerTier): BrokerTierView {
    return BrokerTierView(
        codeText = item.code,
        nameText = truncate(item.name, 80),
        notes = item.notes,
        ratioText = "%.2f".format(item.ratio),
        version = item.version.toString(),
        externalRefText = item.externalRef,
        dock = truncate(item.dock, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerTierViews(items: MutableList<BrokerTier>): MutableList<BrokerTierView> {
    val views = mutableListOf<BrokerTierView>()
    for (item in items) {
        views.add(toBrokerTierView(item))
    }
    return views
}

fun sumBrokerTierView(view: BrokerTierView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedRatio + parsedVersion.toDouble()
}
