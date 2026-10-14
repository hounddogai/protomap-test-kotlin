package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient inspection plan. */
data class LenientInspectionPlanView(
    var codeText: String,
    var title: String,
    var country: String,
    var carrier: String,
    var etaMinutes: String,
    var currency: String,
    var source: String,
    var margin: String,
    var active: String,
    var statusLabel: String,
)

fun toLenientInspectionPlanView(item: LenientInspectionPlan): LenientInspectionPlanView {
    return LenientInspectionPlanView(
        codeText = item.code,
        title = item.title,
        country = item.country,
        carrier = item.carrier,
        etaMinutes = item.etaMinutes.toString(),
        currency = item.currency,
        source = truncate(item.source, 64),
        margin = "%.2f".format(item.margin),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientInspectionPlanViews(items: MutableList<LenientInspectionPlan>): MutableList<LenientInspectionPlanView> {
    val views = mutableListOf<LenientInspectionPlanView>()
    for (item in items) {
        views.add(toLenientInspectionPlanView(item))
    }
    return views
}

fun sumLenientInspectionPlanView(view: LenientInspectionPlanView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedEtaMinutes.toDouble() + parsedMargin
}
