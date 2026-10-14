package com.acme.transport.freightpay.dispute.slot

/** How screens and exports show a fallback dispute plan. */
data class FallbackDisputePlanView(
    var code: String,
    var label: String,
    var revisionText: String,
    var windowDaysText: String,
    var maximum: String,
    var etaMinutesText: String,
    var title: String,
    var graceDays: String,
    var attempts: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toFallbackDisputePlanView(item: FallbackDisputePlan): FallbackDisputePlanView {
    return FallbackDisputePlanView(
        code = item.code,
        label = item.label,
        revisionText = item.revision.toString(),
        windowDaysText = item.windowDays.toString(),
        maximum = "%.2f".format(item.maximum),
        etaMinutesText = item.etaMinutes.toString(),
        title = item.title,
        graceDays = item.graceDays.toString(),
        attempts = item.attempts.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDisputePlanViews(items: MutableList<FallbackDisputePlan>): MutableList<FallbackDisputePlanView> {
    val views = mutableListOf<FallbackDisputePlanView>()
    for (item in items) {
        views.add(toFallbackDisputePlanView(item))
    }
    return views
}

fun sumFallbackDisputePlanView(view: FallbackDisputePlanView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedRevision.toDouble() + parsedWindowDays.toDouble() + parsedMaximum
}
