package com.acme.transport.freightpay.ratecard.audit

/** How screens and exports show a fallback rate card template. */
data class FallbackRateCardTemplateView(
    var codeText: String,
    var label: String,
    var discount: String,
    var trailerId: String,
    var externalRefText: String,
    var maximumText: String,
    var retries: String,
    var statusLabel: String,
)

fun toFallbackRateCardTemplateView(item: FallbackRateCardTemplate): FallbackRateCardTemplateView {
    return FallbackRateCardTemplateView(
        codeText = item.code,
        label = item.label,
        discount = "%.2f".format(item.discount),
        trailerId = item.trailerId,
        externalRefText = item.externalRef,
        maximumText = "%.2f".format(item.maximum),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRateCardTemplateViews(items: MutableList<FallbackRateCardTemplate>): MutableList<FallbackRateCardTemplateView> {
    val views = mutableListOf<FallbackRateCardTemplateView>()
    for (item in items) {
        views.add(toFallbackRateCardTemplateView(item))
    }
    return views
}

fun sumFallbackRateCardTemplateView(view: FallbackRateCardTemplateView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedDiscount + parsedMaximum + parsedRetries.toDouble()
}
