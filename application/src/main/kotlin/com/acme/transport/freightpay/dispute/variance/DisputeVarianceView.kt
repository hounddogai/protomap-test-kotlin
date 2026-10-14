package com.acme.transport.freightpay.dispute.variance

/** How screens and exports show a dispute variance. */
data class DisputeVarianceView(
    var codeText: String,
    var name: String,
    var odometer: String,
    var attempts: String,
    var carrier: String,
    var amount: String,
    var weight: String,
    var maximum: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toDisputeVarianceView(item: DisputeVariance): DisputeVarianceView {
    return DisputeVarianceView(
        codeText = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        attempts = item.attempts.toString(),
        carrier = item.carrier,
        amount = "%.2f".format(item.amount),
        weight = "%.2f".format(item.weight),
        maximum = "%.2f".format(item.maximum),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeVarianceViews(items: MutableList<DisputeVariance>): MutableList<DisputeVarianceView> {
    val views = mutableListOf<DisputeVarianceView>()
    for (item in items) {
        views.add(toDisputeVarianceView(item))
    }
    return views
}

fun sumDisputeVarianceView(view: DisputeVarianceView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedOdometer.toDouble() + parsedAttempts.toDouble() + parsedAmount
}
