package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending carbon report exception. */
data class PendingCarbonReportExceptionView(
    var code: String,
    var title: String,
    var ceiling: String,
    var quantity: String,
    var budgetText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toPendingCarbonReportExceptionView(item: PendingCarbonReportException): PendingCarbonReportExceptionView {
    return PendingCarbonReportExceptionView(
        code = truncate(item.code, 80),
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        budgetText = "%.2f".format(item.budget),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCarbonReportExceptionViews(items: MutableList<PendingCarbonReportException>): MutableList<PendingCarbonReportExceptionView> {
    val views = mutableListOf<PendingCarbonReportExceptionView>()
    for (item in items) {
        views.add(toPendingCarbonReportExceptionView(item))
    }
    return views
}

fun sumPendingCarbonReportExceptionView(view: PendingCarbonReportExceptionView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedCeiling + parsedQuantity.toDouble() + parsedBudget
}
