package com.acme.transport.crewing.qualification.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification ledger. */
data class QualificationLedgerView(
    var codeText: String,
    var name: String,
    var pallets: String,
    var ratio: String,
    var floorValue: String,
    var channel: String,
    var statusLabel: String,
)

fun toQualificationLedgerView(item: QualificationLedger): QualificationLedgerView {
    return QualificationLedgerView(
        codeText = item.code,
        name = item.name,
        pallets = item.pallets.toString(),
        ratio = "%.2f".format(item.ratio),
        floorValue = "%.2f".format(item.floorValue),
        channel = truncate(item.channel, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationLedgerViews(items: MutableList<QualificationLedger>): MutableList<QualificationLedgerView> {
    val views = mutableListOf<QualificationLedgerView>()
    for (item in items) {
        views.add(toQualificationLedgerView(item))
    }
    return views
}

fun sumQualificationLedgerView(view: QualificationLedgerView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedPallets.toDouble() + parsedRatio + parsedFloorValue
}
