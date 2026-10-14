package com.acme.transport.safety.violation.primaryviolationquota

/** How screens and exports show a lenient violation quota. */
data class LenientViolationQuotaView(
    var codeText: String,
    var title: String,
    var rateText: String,
    var carrier: String,
    var attempts: String,
    var depot: String,
    var minimum: String,
    var retriesText: String,
    var statusLabel: String,
)

fun toLenientViolationQuotaView(item: LenientViolationQuota): LenientViolationQuotaView {
    return LenientViolationQuotaView(
        codeText = item.code,
        title = item.title,
        rateText = "%.2f".format(item.rate),
        carrier = item.carrier,
        attempts = item.attempts.toString(),
        depot = item.depot,
        minimum = "%.2f".format(item.minimum),
        retriesText = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientViolationQuotaViews(items: MutableList<LenientViolationQuota>): MutableList<LenientViolationQuotaView> {
    val views = mutableListOf<LenientViolationQuotaView>()
    for (item in items) {
        views.add(toLenientViolationQuotaView(item))
    }
    return views
}

fun sumLenientViolationQuotaView(view: LenientViolationQuotaView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedRate + parsedAttempts.toDouble() + parsedMinimum
}
