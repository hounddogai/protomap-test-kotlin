package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched roster agreement. */
data class BatchedRosterAgreementView(
    var code: String,
    var titleText: String,
    var driverId: String,
    var discount: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toBatchedRosterAgreementView(item: BatchedRosterAgreement): BatchedRosterAgreementView {
    return BatchedRosterAgreementView(
        code = truncate(item.code, 64),
        titleText = item.title,
        driverId = item.driverId,
        discount = "%.2f".format(item.discount),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRosterAgreementViews(items: MutableList<BatchedRosterAgreement>): MutableList<BatchedRosterAgreementView> {
    val views = mutableListOf<BatchedRosterAgreementView>()
    for (item in items) {
        views.add(toBatchedRosterAgreementView(item))
    }
    return views
}

fun sumBatchedRosterAgreementView(view: BatchedRosterAgreementView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedDiscount + parsedMinimum
}
