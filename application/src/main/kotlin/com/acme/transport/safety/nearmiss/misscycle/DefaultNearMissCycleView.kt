package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default near miss cycle. */
data class DefaultNearMissCycleView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var maximum: String,
    var quantity: String,
    var version: String,
    var summary: String,
    var sourceText: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toDefaultNearMissCycleView(item: DefaultNearMissCycle): DefaultNearMissCycleView {
    return DefaultNearMissCycleView(
        code = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        maximum = "%.2f".format(item.maximum),
        quantity = item.quantity.toString(),
        version = item.version.toString(),
        summary = item.summary,
        sourceText = item.source,
        timezoneText = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNearMissCycleViews(items: MutableList<DefaultNearMissCycle>): MutableList<DefaultNearMissCycleView> {
    val views = mutableListOf<DefaultNearMissCycleView>()
    for (item in items) {
        views.add(toDefaultNearMissCycleView(item))
    }
    return views
}

fun sumDefaultNearMissCycleView(view: DefaultNearMissCycleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedEtaMinutes.toDouble() + parsedMaximum + parsedQuantity.toDouble()
}
