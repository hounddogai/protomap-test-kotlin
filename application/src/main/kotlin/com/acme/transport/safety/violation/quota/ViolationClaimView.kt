package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation claim. */
data class ViolationClaimView(
    var codeText: String,
    var title: String,
    var description: String,
    var startsAt: String,
    var visible: String,
    var unit: String,
    var maximumText: String,
    var odometer: String,
    var batchSize: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toViolationClaimView(item: ViolationClaim): ViolationClaimView {
    return ViolationClaimView(
        codeText = item.code,
        title = truncate(item.title, 120),
        description = item.description,
        startsAt = item.startsAt.toString(),
        visible = "${item.visible}",
        unit = truncate(item.unit, 80),
        maximumText = "%.2f".format(item.maximum),
        odometer = item.odometer.toString(),
        batchSize = item.batchSize.toString(),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationClaimViews(items: MutableList<ViolationClaim>): MutableList<ViolationClaimView> {
    val views = mutableListOf<ViolationClaimView>()
    for (item in items) {
        views.add(toViolationClaimView(item))
    }
    return views
}

fun sumViolationClaimView(view: ViolationClaimView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedStartsAt.toDouble() + parsedMaximum + parsedOdometer.toDouble()
}
