package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual broker tier. */
data class ManualBrokerTierView(
    var codeText: String,
    var label: String,
    var externalRef: String,
    var visibleText: String,
    var endsAt: String,
    var costText: String,
    var summary: String,
    var statusLabel: String,
)

fun toManualBrokerTierView(item: ManualBrokerTier): ManualBrokerTierView {
    return ManualBrokerTierView(
        codeText = item.code,
        label = item.label,
        externalRef = item.externalRef,
        visibleText = "${item.visible}",
        endsAt = item.endsAt.toString(),
        costText = "%.2f".format(item.cost),
        summary = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualBrokerTierViews(items: MutableList<ManualBrokerTier>): MutableList<ManualBrokerTierView> {
    val views = mutableListOf<ManualBrokerTierView>()
    for (item in items) {
        views.add(toManualBrokerTierView(item))
    }
    return views
}

fun sumManualBrokerTierView(view: ManualBrokerTierView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedEndsAt.toDouble() + parsedCost
}
