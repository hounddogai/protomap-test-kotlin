package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped trailer preference. */
data class CappedTrailerPreferenceView(
    var codeText: String,
    var title: String,
    var visible: String,
    var ceiling: String,
    var cost: String,
    var priorityText: String,
    var checksum: String,
    var name: String,
    var region: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toCappedTrailerPreferenceView(item: CappedTrailerPreference): CappedTrailerPreferenceView {
    return CappedTrailerPreferenceView(
        codeText = truncate(item.code, 40),
        title = item.title,
        visible = "${item.visible}",
        ceiling = "%.2f".format(item.ceiling),
        cost = "%.2f".format(item.cost),
        priorityText = item.priority.toString(),
        checksum = truncate(item.checksum, 64),
        name = truncate(item.name, 64),
        region = item.region,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTrailerPreferenceViews(items: MutableList<CappedTrailerPreference>): MutableList<CappedTrailerPreferenceView> {
    val views = mutableListOf<CappedTrailerPreferenceView>()
    for (item in items) {
        views.add(toCappedTrailerPreferenceView(item))
    }
    return views
}

fun sumCappedTrailerPreferenceView(view: CappedTrailerPreferenceView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedCeiling + parsedCost + parsedPriority.toDouble()
}
