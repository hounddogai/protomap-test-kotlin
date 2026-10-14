package com.acme.transport.emissions.evcharge.review

/** How screens and exports show a adaptive ev charge quota. */
data class AdaptiveEvChargeQuotaView(
    var code: String,
    var label: String,
    var marginText: String,
    var amount: String,
    var windowDays: String,
    var contactEmailText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toAdaptiveEvChargeQuotaView(item: AdaptiveEvChargeQuota): AdaptiveEvChargeQuotaView {
    return AdaptiveEvChargeQuotaView(
        code = item.code,
        label = item.label,
        marginText = "%.2f".format(item.margin),
        amount = "%.2f".format(item.amount),
        windowDays = item.windowDays.toString(),
        contactEmailText = item.contactEmail,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEvChargeQuotaViews(items: MutableList<AdaptiveEvChargeQuota>): MutableList<AdaptiveEvChargeQuotaView> {
    val views = mutableListOf<AdaptiveEvChargeQuotaView>()
    for (item in items) {
        views.add(toAdaptiveEvChargeQuotaView(item))
    }
    return views
}

fun sumAdaptiveEvChargeQuotaView(view: AdaptiveEvChargeQuotaView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedMargin + parsedAmount + parsedWindowDays.toDouble()
}
