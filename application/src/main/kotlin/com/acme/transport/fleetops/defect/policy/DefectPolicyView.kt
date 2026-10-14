package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect policy. */
data class DefectPolicyView(
    var codeText: String,
    var title: String,
    var amountText: String,
    var weight: String,
    var routeCodeText: String,
    var currency: String,
    var minimum: String,
    var loadKgText: String,
    var thresholdText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toDefectPolicyView(item: DefectPolicy): DefectPolicyView {
    return DefectPolicyView(
        codeText = truncate(item.code, 80),
        title = item.title,
        amountText = "%.2f".format(item.amount),
        weight = "%.2f".format(item.weight),
        routeCodeText = truncate(item.routeCode, 80),
        currency = item.currency,
        minimum = "%.2f".format(item.minimum),
        loadKgText = "%.2f".format(item.loadKg),
        thresholdText = "%.2f".format(item.threshold),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectPolicyViews(items: MutableList<DefectPolicy>): MutableList<DefectPolicyView> {
    val views = mutableListOf<DefectPolicyView>()
    for (item in items) {
        views.add(toDefectPolicyView(item))
    }
    return views
}

fun sumDefectPolicyView(view: DefectPolicyView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAmount + parsedWeight + parsedMinimum
}
