package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff cycle. */
data class CutoffCycleView(
    var codeText: String,
    var label: String,
    var name: String,
    var locale: String,
    var title: String,
    var quantity: String,
    var statusLabel: String,
)

fun toCutoffCycleView(item: CutoffCycle): CutoffCycleView {
    return CutoffCycleView(
        codeText = truncate(item.code, 120),
        label = item.label,
        name = truncate(item.name, 64),
        locale = item.locale,
        title = item.title,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffCycleViews(items: MutableList<CutoffCycle>): MutableList<CutoffCycleView> {
    val views = mutableListOf<CutoffCycleView>()
    for (item in items) {
        views.add(toCutoffCycleView(item))
    }
    return views
}

fun sumCutoffCycleView(view: CutoffCycleView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
