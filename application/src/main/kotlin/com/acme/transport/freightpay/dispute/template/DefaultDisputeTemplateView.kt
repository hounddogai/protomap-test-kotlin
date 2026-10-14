package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default dispute template. */
data class DefaultDisputeTemplateView(
    var codeText: String,
    var title: String,
    var externalRef: String,
    var ownerIdText: String,
    var sequence: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toDefaultDisputeTemplateView(item: DefaultDisputeTemplate): DefaultDisputeTemplateView {
    return DefaultDisputeTemplateView(
        codeText = truncate(item.code, 40),
        title = truncate(item.title, 120),
        externalRef = truncate(item.externalRef, 120),
        ownerIdText = item.ownerId,
        sequence = item.sequence.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDisputeTemplateViews(items: MutableList<DefaultDisputeTemplate>): MutableList<DefaultDisputeTemplateView> {
    val views = mutableListOf<DefaultDisputeTemplateView>()
    for (item in items) {
        views.add(toDefaultDisputeTemplateView(item))
    }
    return views
}

fun sumDefaultDisputeTemplateView(view: DefaultDisputeTemplateView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedSequence.toDouble() + parsedCeiling
}
