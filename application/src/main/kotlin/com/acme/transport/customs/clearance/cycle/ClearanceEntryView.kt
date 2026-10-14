package com.acme.transport.customs.clearance.cycle

/** How screens and exports show a clearance entry. */
data class ClearanceEntryView(
    var codeText: String,
    var name: String,
    var lane: String,
    var active: String,
    var checksum: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toClearanceEntryView(item: ClearanceEntry): ClearanceEntryView {
    return ClearanceEntryView(
        codeText = item.code,
        name = item.name,
        lane = item.lane,
        active = "${item.active}",
        checksum = item.checksum,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceEntryViews(items: MutableList<ClearanceEntry>): MutableList<ClearanceEntryView> {
    val views = mutableListOf<ClearanceEntryView>()
    for (item in items) {
        views.add(toClearanceEntryView(item))
    }
    return views
}

fun sumClearanceEntryView(view: ClearanceEntryView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
