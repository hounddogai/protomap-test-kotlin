package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery agreement. */
data class RedeliveryAgreementView(
    var codeText: String,
    var title: String,
    var limit: String,
    var ceilingText: String,
    var lane: String,
    var source: String,
    var channel: String,
    var createdAt: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toRedeliveryAgreementView(item: RedeliveryAgreement): RedeliveryAgreementView {
    return RedeliveryAgreementView(
        codeText = item.code,
        title = item.title,
        limit = item.limit.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        lane = item.lane,
        source = item.source,
        channel = truncate(item.channel, 40),
        createdAt = item.createdAt.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryAgreementViews(items: MutableList<RedeliveryAgreement>): MutableList<RedeliveryAgreementView> {
    val views = mutableListOf<RedeliveryAgreementView>()
    for (item in items) {
        views.add(toRedeliveryAgreementView(item))
    }
    return views
}

fun sumRedeliveryAgreementView(view: RedeliveryAgreementView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedLimit.toDouble() + parsedCeiling + parsedCreatedAt.toDouble()
}
