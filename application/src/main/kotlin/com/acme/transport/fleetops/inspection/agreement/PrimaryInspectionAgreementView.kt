package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary inspection agreement. */
data class PrimaryInspectionAgreementView(
    var code: String,
    var labelText: String,
    var sourceText: String,
    var description: String,
    var startsAt: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toPrimaryInspectionAgreementView(item: PrimaryInspectionAgreement): PrimaryInspectionAgreementView {
    return PrimaryInspectionAgreementView(
        code = item.code,
        labelText = item.label,
        sourceText = item.source,
        description = item.description,
        startsAt = item.startsAt.toString(),
        routeCode = truncate(item.routeCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryInspectionAgreementViews(items: MutableList<PrimaryInspectionAgreement>): MutableList<PrimaryInspectionAgreementView> {
    val views = mutableListOf<PrimaryInspectionAgreementView>()
    for (item in items) {
        views.add(toPrimaryInspectionAgreementView(item))
    }
    return views
}

fun sumPrimaryInspectionAgreementView(view: PrimaryInspectionAgreementView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
