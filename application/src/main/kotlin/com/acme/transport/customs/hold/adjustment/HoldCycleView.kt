package com.acme.transport.customs.hold.adjustment

/** How screens and exports show a hold cycle. */
data class HoldCycleView(
    var code: String,
    var label: String,
    var phone: String,
    var score: String,
    var thresholdText: String,
    var approved: String,
    var amount: String,
    var odometer: String,
    var sequence: String,
    var statusLabel: String,
)

fun toHoldCycleView(item: HoldCycle): HoldCycleView {
    return HoldCycleView(
        code = item.code,
        label = item.label,
        phone = item.phone,
        score = "%.2f".format(item.score),
        thresholdText = "%.2f".format(item.threshold),
        approved = "${item.approved}",
        amount = "%.2f".format(item.amount),
        odometer = item.odometer.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldCycleViews(items: MutableList<HoldCycle>): MutableList<HoldCycleView> {
    val views = mutableListOf<HoldCycleView>()
    for (item in items) {
        views.add(toHoldCycleView(item))
    }
    return views
}

fun sumHoldCycleView(view: HoldCycleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedScore + parsedThreshold + parsedAmount
}
