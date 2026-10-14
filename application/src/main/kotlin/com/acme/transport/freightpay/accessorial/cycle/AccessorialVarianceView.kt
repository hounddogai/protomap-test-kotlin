package com.acme.transport.freightpay.accessorial.cycle

/** How screens and exports show a accessorial variance. */
data class AccessorialVarianceView(
    var code: String,
    var label: String,
    var approved: String,
    var limit: String,
    var ratio: String,
    var windowDaysText: String,
    var rate: String,
    var threshold: String,
    var weight: String,
    var statusLabel: String,
)

fun toAccessorialVarianceView(item: AccessorialVariance): AccessorialVarianceView {
    return AccessorialVarianceView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        limit = item.limit.toString(),
        ratio = "%.2f".format(item.ratio),
        windowDaysText = item.windowDays.toString(),
        rate = "%.2f".format(item.rate),
        threshold = "%.2f".format(item.threshold),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialVarianceViews(items: MutableList<AccessorialVariance>): MutableList<AccessorialVarianceView> {
    val views = mutableListOf<AccessorialVarianceView>()
    for (item in items) {
        views.add(toAccessorialVarianceView(item))
    }
    return views
}

fun sumAccessorialVarianceView(view: AccessorialVarianceView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedLimit.toDouble() + parsedRatio + parsedWindowDays.toDouble()
}
