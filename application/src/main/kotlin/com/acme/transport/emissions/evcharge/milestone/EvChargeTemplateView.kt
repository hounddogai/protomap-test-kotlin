package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge template. */
data class EvChargeTemplateView(
    var code: String,
    var nameText: String,
    var updatedAtText: String,
    var weight: String,
    var lane: String,
    var shiftCode: String,
    var etaMinutes: String,
    var stopCountText: String,
    var quantity: String,
    var timezone: String,
    var visible: String,
    var statusLabel: String,
)

fun toEvChargeTemplateView(item: EvChargeTemplate): EvChargeTemplateView {
    return EvChargeTemplateView(
        code = item.code,
        nameText = truncate(item.name, 120),
        updatedAtText = item.updatedAt.toString(),
        weight = "%.2f".format(item.weight),
        lane = truncate(item.lane, 64),
        shiftCode = truncate(item.shiftCode, 40),
        etaMinutes = item.etaMinutes.toString(),
        stopCountText = item.stopCount.toString(),
        quantity = item.quantity.toString(),
        timezone = item.timezone,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeTemplateViews(items: MutableList<EvChargeTemplate>): MutableList<EvChargeTemplateView> {
    val views = mutableListOf<EvChargeTemplateView>()
    for (item in items) {
        views.add(toEvChargeTemplateView(item))
    }
    return views
}

fun sumEvChargeTemplateView(view: EvChargeTemplateView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedUpdatedAt.toDouble() + parsedWeight + parsedEtaMinutes.toDouble()
}
