package com.acme.transport.lastmile.proofofdelivery.assignment

/** How screens and exports show a proof of delivery metric. */
data class ProofOfDeliveryMetricView(
    var codeText: String,
    var name: String,
    var summaryText: String,
    var shiftCode: String,
    var maximum: String,
    var active: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryMetricView(item: ProofOfDeliveryMetric): ProofOfDeliveryMetricView {
    return ProofOfDeliveryMetricView(
        codeText = item.code,
        name = item.name,
        summaryText = item.summary,
        shiftCode = item.shiftCode,
        maximum = "%.2f".format(item.maximum),
        active = "${item.active}",
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryMetricViews(items: MutableList<ProofOfDeliveryMetric>): MutableList<ProofOfDeliveryMetricView> {
    val views = mutableListOf<ProofOfDeliveryMetricView>()
    for (item in items) {
        views.add(toProofOfDeliveryMetricView(item))
    }
    return views
}

fun sumProofOfDeliveryMetricView(view: ProofOfDeliveryMetricView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedMaximum + parsedGraceDays.toDouble()
}
