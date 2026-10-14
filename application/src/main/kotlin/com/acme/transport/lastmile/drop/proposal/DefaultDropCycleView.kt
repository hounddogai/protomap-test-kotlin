package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default drop cycle. */
data class DefaultDropCycleView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var trailerId: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toDefaultDropCycleView(item: DefaultDropCycle): DefaultDropCycleView {
    return DefaultDropCycleView(
        code = truncate(item.code, 64),
        labelText = item.label,
        ratio = "%.2f".format(item.ratio),
        trailerId = item.trailerId,
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDropCycleViews(items: MutableList<DefaultDropCycle>): MutableList<DefaultDropCycleView> {
    val views = mutableListOf<DefaultDropCycleView>()
    for (item in items) {
        views.add(toDefaultDropCycleView(item))
    }
    return views
}

fun sumDefaultDropCycleView(view: DefaultDropCycleView): Double {
    val parsedRatio = view.ratio.toDouble()
    return parsedRatio
}
