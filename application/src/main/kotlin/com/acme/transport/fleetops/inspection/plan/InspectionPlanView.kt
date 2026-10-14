package com.acme.transport.fleetops.inspection.plan

/** How screens and exports show a inspection plan. */
data class InspectionPlanView(
    var codeText: String,
    var name: String,
    var maximum: String,
    var startsAtText: String,
    var etaMinutesText: String,
    var weight: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toInspectionPlanView(item: InspectionPlan): InspectionPlanView {
    return InspectionPlanView(
        codeText = item.code,
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        startsAtText = item.startsAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        weight = "%.2f".format(item.weight),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionPlanViews(items: MutableList<InspectionPlan>): MutableList<InspectionPlanView> {
    val views = mutableListOf<InspectionPlanView>()
    for (item in items) {
        views.add(toInspectionPlanView(item))
    }
    return views
}

fun sumInspectionPlanView(view: InspectionPlanView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedMaximum + parsedStartsAt.toDouble() + parsedEtaMinutes.toDouble()
}
