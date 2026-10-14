package com.acme.transport.customs.broker.threshold

/** How screens and exports show a pending broker draft. */
data class PendingBrokerDraftView(
    var code: String,
    var label: String,
    var endsAt: String,
    var active: String,
    var margin: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toPendingBrokerDraftView(item: PendingBrokerDraft): PendingBrokerDraftView {
    return PendingBrokerDraftView(
        code = item.code,
        label = item.label,
        endsAt = item.endsAt.toString(),
        active = "${item.active}",
        margin = "%.2f".format(item.margin),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBrokerDraftViews(items: MutableList<PendingBrokerDraft>): MutableList<PendingBrokerDraftView> {
    val views = mutableListOf<PendingBrokerDraftView>()
    for (item in items) {
        views.add(toPendingBrokerDraftView(item))
    }
    return views
}

fun sumPendingBrokerDraftView(view: PendingBrokerDraftView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedEndsAt.toDouble() + parsedMargin
}
