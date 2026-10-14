package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient cross dock preference. */
data class LenientCrossDockPreferenceView(
    var codeText: String,
    var title: String,
    var labelText: String,
    var cost: String,
    var weightText: String,
    var statusLabel: String,
)

fun toLenientCrossDockPreferenceView(item: LenientCrossDockPreference): LenientCrossDockPreferenceView {
    return LenientCrossDockPreferenceView(
        codeText = item.code,
        title = truncate(item.title, 80),
        labelText = item.label,
        cost = "%.2f".format(item.cost),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCrossDockPreferenceViews(items: MutableList<LenientCrossDockPreference>): MutableList<LenientCrossDockPreferenceView> {
    val views = mutableListOf<LenientCrossDockPreferenceView>()
    for (item in items) {
        views.add(toLenientCrossDockPreferenceView(item))
    }
    return views
}

fun sumLenientCrossDockPreferenceView(view: LenientCrossDockPreferenceView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedCost + parsedWeight
}
