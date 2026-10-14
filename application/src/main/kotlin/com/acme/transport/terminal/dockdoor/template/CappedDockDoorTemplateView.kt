package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dock door template. */
data class CappedDockDoorTemplateView(
    var codeText: String,
    var label: String,
    var contactEmailText: String,
    var archived: String,
    var thresholdText: String,
    var name: String,
    var quantity: String,
    var currencyText: String,
    var channel: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toCappedDockDoorTemplateView(item: CappedDockDoorTemplate): CappedDockDoorTemplateView {
    return CappedDockDoorTemplateView(
        codeText = item.code,
        label = item.label,
        contactEmailText = item.contactEmail,
        archived = "${item.archived}",
        thresholdText = "%.2f".format(item.threshold),
        name = item.name,
        quantity = item.quantity.toString(),
        currencyText = item.currency,
        channel = truncate(item.channel, 80),
        reviewer = truncate(item.reviewer, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDockDoorTemplateViews(items: MutableList<CappedDockDoorTemplate>): MutableList<CappedDockDoorTemplateView> {
    val views = mutableListOf<CappedDockDoorTemplateView>()
    for (item in items) {
        views.add(toCappedDockDoorTemplateView(item))
    }
    return views
}

fun sumCappedDockDoorTemplateView(view: CappedDockDoorTemplateView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedThreshold + parsedQuantity.toDouble()
}
