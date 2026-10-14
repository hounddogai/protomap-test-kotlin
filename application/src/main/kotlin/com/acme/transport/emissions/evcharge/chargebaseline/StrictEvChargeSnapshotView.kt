package com.acme.transport.emissions.evcharge.chargebaseline

/** How screens and exports show a strict ev charge snapshot. */
data class StrictEvChargeSnapshotView(
    var code: String,
    var name: String,
    var shiftCode: String,
    var lane: String,
    var locale: String,
    var batchSize: String,
    var currencyText: String,
    var unit: String,
    var statusLabel: String,
)

fun toStrictEvChargeSnapshotView(item: StrictEvChargeSnapshot): StrictEvChargeSnapshotView {
    return StrictEvChargeSnapshotView(
        code = item.code,
        name = item.name,
        shiftCode = item.shiftCode,
        lane = item.lane,
        locale = item.locale,
        batchSize = item.batchSize.toString(),
        currencyText = item.currency,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEvChargeSnapshotViews(items: MutableList<StrictEvChargeSnapshot>): MutableList<StrictEvChargeSnapshotView> {
    val views = mutableListOf<StrictEvChargeSnapshotView>()
    for (item in items) {
        views.add(toStrictEvChargeSnapshotView(item))
    }
    return views
}

fun sumStrictEvChargeSnapshotView(view: StrictEvChargeSnapshotView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
