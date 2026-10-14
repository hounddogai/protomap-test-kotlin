package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation allocation. */
data class SortationAllocationView(
    var code: String,
    var name: String,
    var marginText: String,
    var locale: String,
    var active: String,
    var visible: String,
    var currency: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toSortationAllocationView(item: SortationAllocation): SortationAllocationView {
    return SortationAllocationView(
        code = item.code,
        name = item.name,
        marginText = "%.2f".format(item.margin),
        locale = item.locale,
        active = "${item.active}",
        visible = "${item.visible}",
        currency = truncate(item.currency, 120),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationAllocationViews(items: MutableList<SortationAllocation>): MutableList<SortationAllocationView> {
    val views = mutableListOf<SortationAllocationView>()
    for (item in items) {
        views.add(toSortationAllocationView(item))
    }
    return views
}

fun sumSortationAllocationView(view: SortationAllocationView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedMargin + parsedRatio
}
