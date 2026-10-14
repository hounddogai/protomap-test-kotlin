package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped dispute agreement. */
data class CappedDisputeAgreementView(
    var code: String,
    var titleText: String,
    var graceDays: String,
    var phone: String,
    var score: String,
    var etaMinutes: String,
    var createdAt: String,
    var category: String,
    var carrierText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toCappedDisputeAgreementView(item: CappedDisputeAgreement): CappedDisputeAgreementView {
    return CappedDisputeAgreementView(
        code = item.code,
        titleText = item.title,
        graceDays = item.graceDays.toString(),
        phone = truncate(item.phone, 80),
        score = "%.2f".format(item.score),
        etaMinutes = item.etaMinutes.toString(),
        createdAt = item.createdAt.toString(),
        category = item.category,
        carrierText = truncate(item.carrier, 40),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDisputeAgreementViews(items: MutableList<CappedDisputeAgreement>): MutableList<CappedDisputeAgreementView> {
    val views = mutableListOf<CappedDisputeAgreementView>()
    for (item in items) {
        views.add(toCappedDisputeAgreementView(item))
    }
    return views
}

fun sumCappedDisputeAgreementView(view: CappedDisputeAgreementView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedScore = view.score.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedGraceDays.toDouble() + parsedScore + parsedEtaMinutes.toDouble()
}
