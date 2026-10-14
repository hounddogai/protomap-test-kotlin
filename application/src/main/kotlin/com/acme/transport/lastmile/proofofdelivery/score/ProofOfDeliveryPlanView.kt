package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery plan. */
data class ProofOfDeliveryPlanView(
    var codeText: String,
    var title: String,
    var stopCount: String,
    var dock: String,
    var floorValueText: String,
    var currencyText: String,
    var batchSize: String,
    var weight: String,
    var cost: String,
    var priority: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryPlanView(item: ProofOfDeliveryPlan): ProofOfDeliveryPlanView {
    return ProofOfDeliveryPlanView(
        codeText = item.code,
        title = truncate(item.title, 40),
        stopCount = item.stopCount.toString(),
        dock = item.dock,
        floorValueText = "%.2f".format(item.floorValue),
        currencyText = item.currency,
        batchSize = item.batchSize.toString(),
        weight = "%.2f".format(item.weight),
        cost = "%.2f".format(item.cost),
        priority = item.priority.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryPlanViews(items: MutableList<ProofOfDeliveryPlan>): MutableList<ProofOfDeliveryPlanView> {
    val views = mutableListOf<ProofOfDeliveryPlanView>()
    for (item in items) {
        views.add(toProofOfDeliveryPlanView(item))
    }
    return views
}

fun sumProofOfDeliveryPlanView(view: ProofOfDeliveryPlanView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedStopCount.toDouble() + parsedFloorValue + parsedBatchSize.toDouble()
}
