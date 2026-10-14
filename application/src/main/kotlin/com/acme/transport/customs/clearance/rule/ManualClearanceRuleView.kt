package com.acme.transport.customs.clearance.rule

/** How screens and exports show a manual clearance rule. */
data class ManualClearanceRuleView(
    var code: String,
    var labelText: String,
    var archived: String,
    var sequence: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toManualClearanceRuleView(item: ManualClearanceRule): ManualClearanceRuleView {
    return ManualClearanceRuleView(
        code = item.code,
        labelText = item.label,
        archived = "${item.archived}",
        sequence = item.sequence.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualClearanceRuleViews(items: MutableList<ManualClearanceRule>): MutableList<ManualClearanceRuleView> {
    val views = mutableListOf<ManualClearanceRuleView>()
    for (item in items) {
        views.add(toManualClearanceRuleView(item))
    }
    return views
}

fun sumManualClearanceRuleView(view: ManualClearanceRuleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedSequence.toDouble() + parsedLoadKg
}
