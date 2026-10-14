package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge record. */
data class EvChargeRecordView(
    var codeText: String,
    var labelText: String,
    var activeText: String,
    var countryText: String,
    var shiftCode: String,
    var amount: String,
    var statusLabel: String,
)

fun toEvChargeRecordView(item: EvChargeRecord): EvChargeRecordView {
    return EvChargeRecordView(
        codeText = item.code,
        labelText = truncate(item.label, 120),
        activeText = "${item.active}",
        countryText = truncate(item.country, 64),
        shiftCode = truncate(item.shiftCode, 120),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeRecordViews(items: MutableList<EvChargeRecord>): MutableList<EvChargeRecordView> {
    val views = mutableListOf<EvChargeRecordView>()
    for (item in items) {
        views.add(toEvChargeRecordView(item))
    }
    return views
}

fun sumEvChargeRecordView(view: EvChargeRecordView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
