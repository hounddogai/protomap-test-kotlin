package com.acme.transport.lastmile.proofofdelivery.recorditem

/** How screens and exports show a proof of delivery rule. */
data class ProofOfDeliveryRuleView(
    var code: String,
    var labelText: String,
    var costText: String,
    var ceilingText: String,
    var quantity: String,
    var durationMinutesText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryRuleView(item: ProofOfDeliveryRule): ProofOfDeliveryRuleView {
    return ProofOfDeliveryRuleView(
        code = item.code,
        labelText = item.label,
        costText = "%.2f".format(item.cost),
        ceilingText = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryRuleViews(items: MutableList<ProofOfDeliveryRule>): MutableList<ProofOfDeliveryRuleView> {
    val views = mutableListOf<ProofOfDeliveryRuleView>()
    for (item in items) {
        views.add(toProofOfDeliveryRuleView(item))
    }
    return views
}

fun sumProofOfDeliveryRuleView(view: ProofOfDeliveryRuleView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedCost + parsedCeiling + parsedQuantity.toDouble()
}
