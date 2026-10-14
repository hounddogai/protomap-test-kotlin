package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul template. */
data class BackhaulTemplateView(
    var codeText: String,
    var name: String,
    var currency: String,
    var windowDays: String,
    var ceiling: String,
    var sequence: String,
    var ownerIdText: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toBackhaulTemplateView(item: BackhaulTemplate): BackhaulTemplateView {
    return BackhaulTemplateView(
        codeText = truncate(item.code, 64),
        name = item.name,
        currency = truncate(item.currency, 64),
        windowDays = item.windowDays.toString(),
        ceiling = "%.2f".format(item.ceiling),
        sequence = item.sequence.toString(),
        ownerIdText = item.ownerId,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulTemplateViews(items: MutableList<BackhaulTemplate>): MutableList<BackhaulTemplateView> {
    val views = mutableListOf<BackhaulTemplateView>()
    for (item in items) {
        views.add(toBackhaulTemplateView(item))
    }
    return views
}

fun sumBackhaulTemplateView(view: BackhaulTemplateView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedWindowDays.toDouble() + parsedCeiling + parsedSequence.toDouble()
}
