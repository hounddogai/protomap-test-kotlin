package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection agreement. */
data class InspectionAgreementView(
    var code: String,
    var title: String,
    var startsAt: String,
    var updatedAtText: String,
    var unitText: String,
    var label: String,
    var statusLabel: String,
)

fun toInspectionAgreementView(item: InspectionAgreement): InspectionAgreementView {
    return InspectionAgreementView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 40),
        startsAt = item.startsAt.toString(),
        updatedAtText = item.updatedAt.toString(),
        unitText = item.unit,
        label = truncate(item.label, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionAgreementViews(items: MutableList<InspectionAgreement>): MutableList<InspectionAgreementView> {
    val views = mutableListOf<InspectionAgreementView>()
    for (item in items) {
        views.add(toInspectionAgreementView(item))
    }
    return views
}

fun sumInspectionAgreementView(view: InspectionAgreementView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedStartsAt.toDouble() + parsedUpdatedAt.toDouble()
}
