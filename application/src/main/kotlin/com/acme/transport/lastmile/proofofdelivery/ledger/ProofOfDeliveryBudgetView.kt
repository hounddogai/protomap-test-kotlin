package com.acme.transport.lastmile.proofofdelivery.ledger

/** How screens and exports show a proof of delivery budget. */
data class ProofOfDeliveryBudgetView(
    var code: String,
    var name: String,
    var score: String,
    var trailerId: String,
    var carrier: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryBudgetView(item: ProofOfDeliveryBudget): ProofOfDeliveryBudgetView {
    return ProofOfDeliveryBudgetView(
        code = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        trailerId = item.trailerId,
        carrier = item.carrier,
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryBudgetViews(items: MutableList<ProofOfDeliveryBudget>): MutableList<ProofOfDeliveryBudgetView> {
    val views = mutableListOf<ProofOfDeliveryBudgetView>()
    for (item in items) {
        views.add(toProofOfDeliveryBudgetView(item))
    }
    return views
}

fun sumProofOfDeliveryBudgetView(view: ProofOfDeliveryBudgetView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
