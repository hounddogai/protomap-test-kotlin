package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary qualification ledger. */
data class PrimaryQualificationLedgerView(
    var code: String,
    var title: String,
    var vehicleIdText: String,
    var quantityText: String,
    var routeCode: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toPrimaryQualificationLedgerView(item: PrimaryQualificationLedger): PrimaryQualificationLedgerView {
    return PrimaryQualificationLedgerView(
        code = item.code,
        title = truncate(item.title, 120),
        vehicleIdText = item.vehicleId,
        quantityText = item.quantity.toString(),
        routeCode = truncate(item.routeCode, 40),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryQualificationLedgerViews(items: MutableList<PrimaryQualificationLedger>): MutableList<PrimaryQualificationLedgerView> {
    val views = mutableListOf<PrimaryQualificationLedgerView>()
    for (item in items) {
        views.add(toPrimaryQualificationLedgerView(item))
    }
    return views
}

fun sumPrimaryQualificationLedgerView(view: PrimaryQualificationLedgerView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedQuantity.toDouble() + parsedEtaMinutes.toDouble()
}
