package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient ev charge ledger. */
data class LenientEvChargeLedgerView(
    var code: String,
    var label: String,
    var floorValue: String,
    var unitText: String,
    var costText: String,
    var checksum: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toLenientEvChargeLedgerView(item: LenientEvChargeLedger): LenientEvChargeLedgerView {
    return LenientEvChargeLedgerView(
        code = item.code,
        label = item.label,
        floorValue = "%.2f".format(item.floorValue),
        unitText = truncate(item.unit, 80),
        costText = "%.2f".format(item.cost),
        checksum = item.checksum,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientEvChargeLedgerViews(items: MutableList<LenientEvChargeLedger>): MutableList<LenientEvChargeLedgerView> {
    val views = mutableListOf<LenientEvChargeLedgerView>()
    for (item in items) {
        views.add(toLenientEvChargeLedgerView(item))
    }
    return views
}

fun sumLenientEvChargeLedgerView(view: LenientEvChargeLedgerView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedFloorValue + parsedCost + parsedEndsAt.toDouble()
}
