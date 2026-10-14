package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report budget. */
data class CarbonReportBudgetView(
    var code: String,
    var name: String,
    var category: String,
    var threshold: String,
    var floorValue: String,
    var batchSize: String,
    var contactEmail: String,
    var phone: String,
    var descriptionText: String,
    var maximum: String,
    var retries: String,
    var statusLabel: String,
)

fun toCarbonReportBudgetView(item: CarbonReportBudget): CarbonReportBudgetView {
    return CarbonReportBudgetView(
        code = item.code,
        name = item.name,
        category = item.category,
        threshold = "%.2f".format(item.threshold),
        floorValue = "%.2f".format(item.floorValue),
        batchSize = item.batchSize.toString(),
        contactEmail = truncate(item.contactEmail, 64),
        phone = truncate(item.phone, 80),
        descriptionText = item.description,
        maximum = "%.2f".format(item.maximum),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportBudgetViews(items: MutableList<CarbonReportBudget>): MutableList<CarbonReportBudgetView> {
    val views = mutableListOf<CarbonReportBudgetView>()
    for (item in items) {
        views.add(toCarbonReportBudgetView(item))
    }
    return views
}

fun sumCarbonReportBudgetView(view: CarbonReportBudgetView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedThreshold + parsedFloorValue + parsedBatchSize.toDouble()
}
