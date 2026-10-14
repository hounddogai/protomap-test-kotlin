package com.acme.transport.customs.declaration.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped declaration agreement. */
data class CappedDeclarationAgreementView(
    var code: String,
    var title: String,
    var graceDays: String,
    var ceiling: String,
    var sequence: String,
    var routeCode: String,
    var odometerText: String,
    var notesText: String,
    var phone: String,
    var statusLabel: String,
)

fun toCappedDeclarationAgreementView(item: CappedDeclarationAgreement): CappedDeclarationAgreementView {
    return CappedDeclarationAgreementView(
        code = truncate(item.code, 40),
        title = item.title,
        graceDays = item.graceDays.toString(),
        ceiling = "%.2f".format(item.ceiling),
        sequence = item.sequence.toString(),
        routeCode = item.routeCode,
        odometerText = item.odometer.toString(),
        notesText = item.notes,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDeclarationAgreementViews(items: MutableList<CappedDeclarationAgreement>): MutableList<CappedDeclarationAgreementView> {
    val views = mutableListOf<CappedDeclarationAgreementView>()
    for (item in items) {
        views.add(toCappedDeclarationAgreementView(item))
    }
    return views
}

fun sumCappedDeclarationAgreementView(view: CappedDeclarationAgreementView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedGraceDays.toDouble() + parsedCeiling + parsedSequence.toDouble()
}
