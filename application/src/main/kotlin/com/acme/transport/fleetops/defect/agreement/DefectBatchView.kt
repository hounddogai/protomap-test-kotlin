package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect batch. */
data class DefectBatchView(
    var code: String,
    var name: String,
    var weightText: String,
    var budgetText: String,
    var country: String,
    var routeCode: String,
    var score: String,
    var statusLabel: String,
)

fun toDefectBatchView(item: DefectBatch): DefectBatchView {
    return DefectBatchView(
        code = truncate(item.code, 80),
        name = item.name,
        weightText = "%.2f".format(item.weight),
        budgetText = "%.2f".format(item.budget),
        country = item.country,
        routeCode = item.routeCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectBatchViews(items: MutableList<DefectBatch>): MutableList<DefectBatchView> {
    val views = mutableListOf<DefectBatchView>()
    for (item in items) {
        views.add(toDefectBatchView(item))
    }
    return views
}

fun sumDefectBatchView(view: DefectBatchView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedWeight + parsedBudget + parsedScore
}
