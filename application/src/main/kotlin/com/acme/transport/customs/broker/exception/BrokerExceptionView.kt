package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker exception. */
data class BrokerExceptionView(
    var code: String,
    var name: String,
    var pallets: String,
    var segmentText: String,
    var approved: String,
    var thresholdText: String,
    var ratio: String,
    var priority: String,
    var versionText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toBrokerExceptionView(item: BrokerException): BrokerExceptionView {
    return BrokerExceptionView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        pallets = item.pallets.toString(),
        segmentText = item.segment,
        approved = "${item.approved}",
        thresholdText = "%.2f".format(item.threshold),
        ratio = "%.2f".format(item.ratio),
        priority = item.priority.toString(),
        versionText = item.version.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerExceptionViews(items: MutableList<BrokerException>): MutableList<BrokerExceptionView> {
    val views = mutableListOf<BrokerExceptionView>()
    for (item in items) {
        views.add(toBrokerExceptionView(item))
    }
    return views
}

fun sumBrokerExceptionView(view: BrokerExceptionView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedPallets.toDouble() + parsedThreshold + parsedRatio
}
