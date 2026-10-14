package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending node agreement. */
data class PendingNodeAgreementView(
    var codeText: String,
    var label: String,
    var lane: String,
    var phone: String,
    var createdAt: String,
    var checksum: String,
    var amount: String,
    var statusLabel: String,
)

fun toPendingNodeAgreementView(item: PendingNodeAgreement): PendingNodeAgreementView {
    return PendingNodeAgreementView(
        codeText = item.code,
        label = item.label,
        lane = truncate(item.lane, 120),
        phone = truncate(item.phone, 120),
        createdAt = item.createdAt.toString(),
        checksum = item.checksum,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNodeAgreementViews(items: MutableList<PendingNodeAgreement>): MutableList<PendingNodeAgreementView> {
    val views = mutableListOf<PendingNodeAgreementView>()
    for (item in items) {
        views.add(toPendingNodeAgreementView(item))
    }
    return views
}

fun sumPendingNodeAgreementView(view: PendingNodeAgreementView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCreatedAt.toDouble() + parsedAmount
}
