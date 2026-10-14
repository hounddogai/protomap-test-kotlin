package com.acme.transport.emissions.carbonreport.reportmapping

/** How screens and exports show a global carbon report adjustment. */
data class GlobalCarbonReportAdjustmentView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var visible: String,
    var approved: String,
    var minimum: String,
    var summary: String,
    var budget: String,
    var statusLabel: String,
)

fun toGlobalCarbonReportAdjustmentView(item: GlobalCarbonReportAdjustment): GlobalCarbonReportAdjustmentView {
    return GlobalCarbonReportAdjustmentView(
        code = item.code,
        name = item.name,
        expiresAt = item.expiresAt.toString(),
        visible = "${item.visible}",
        approved = "${item.approved}",
        minimum = "%.2f".format(item.minimum),
        summary = item.summary,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCarbonReportAdjustmentViews(items: MutableList<GlobalCarbonReportAdjustment>): MutableList<GlobalCarbonReportAdjustmentView> {
    val views = mutableListOf<GlobalCarbonReportAdjustmentView>()
    for (item in items) {
        views.add(toGlobalCarbonReportAdjustmentView(item))
    }
    return views
}

fun sumGlobalCarbonReportAdjustmentView(view: GlobalCarbonReportAdjustmentView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedExpiresAt.toDouble() + parsedMinimum + parsedBudget
}
