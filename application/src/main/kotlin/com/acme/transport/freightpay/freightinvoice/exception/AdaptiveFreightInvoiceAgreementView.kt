package com.acme.transport.freightpay.freightinvoice.exception

/** How screens and exports show a adaptive freight invoice agreement. */
data class AdaptiveFreightInvoiceAgreementView(
    var code: String,
    var titleText: String,
    var maximum: String,
    var rateText: String,
    var limit: String,
    var statusLabel: String,
)

fun toAdaptiveFreightInvoiceAgreementView(item: AdaptiveFreightInvoiceAgreement): AdaptiveFreightInvoiceAgreementView {
    return AdaptiveFreightInvoiceAgreementView(
        code = item.code,
        titleText = item.title,
        maximum = "%.2f".format(item.maximum),
        rateText = "%.2f".format(item.rate),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveFreightInvoiceAgreementViews(items: MutableList<AdaptiveFreightInvoiceAgreement>): MutableList<AdaptiveFreightInvoiceAgreementView> {
    val views = mutableListOf<AdaptiveFreightInvoiceAgreementView>()
    for (item in items) {
        views.add(toAdaptiveFreightInvoiceAgreementView(item))
    }
    return views
}

fun sumAdaptiveFreightInvoiceAgreementView(view: AdaptiveFreightInvoiceAgreementView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedMaximum + parsedRate + parsedLimit.toDouble()
}
