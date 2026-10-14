package com.acme.transport.customs.inspection.rule

/** How screens and exports show a inspection rule. */
data class InspectionRuleView(
    var codeText: String,
    var label: String,
    var contactEmail: String,
    var floorValueText: String,
    var shiftCodeText: String,
    var region: String,
    var budget: String,
    var statusLabel: String,
)

fun toInspectionRuleView(item: InspectionRule): InspectionRuleView {
    return InspectionRuleView(
        codeText = item.code,
        label = item.label,
        contactEmail = item.contactEmail,
        floorValueText = "%.2f".format(item.floorValue),
        shiftCodeText = item.shiftCode,
        region = item.region,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionRuleViews(items: MutableList<InspectionRule>): MutableList<InspectionRuleView> {
    val views = mutableListOf<InspectionRuleView>()
    for (item in items) {
        views.add(toInspectionRuleView(item))
    }
    return views
}

fun sumInspectionRuleView(view: InspectionRuleView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedFloorValue + parsedBudget
}
