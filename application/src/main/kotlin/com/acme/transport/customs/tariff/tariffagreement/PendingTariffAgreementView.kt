package com.acme.transport.customs.tariff.tariffagreement

/** How screens and exports show a pending tariff agreement. */
data class PendingTariffAgreementView(
    var code: String,
    var title: String,
    var currencyText: String,
    var segmentText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toPendingTariffAgreementView(item: PendingTariffAgreement): PendingTariffAgreementView {
    return PendingTariffAgreementView(
        code = item.code,
        title = item.title,
        currencyText = item.currency,
        segmentText = item.segment,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTariffAgreementViews(items: MutableList<PendingTariffAgreement>): MutableList<PendingTariffAgreementView> {
    val views = mutableListOf<PendingTariffAgreementView>()
    for (item in items) {
        views.add(toPendingTariffAgreementView(item))
    }
    return views
}

fun sumPendingTariffAgreementView(view: PendingTariffAgreementView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
