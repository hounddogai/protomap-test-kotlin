package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary camera event digest. */
data class PrimaryCameraEventDigestView(
    var code: String,
    var titleText: String,
    var amount: String,
    var locale: String,
    var startsAt: String,
    var externalRef: String,
    var sequence: String,
    var statusLabel: String,
)

fun toPrimaryCameraEventDigestView(item: PrimaryCameraEventDigest): PrimaryCameraEventDigestView {
    return PrimaryCameraEventDigestView(
        code = item.code,
        titleText = truncate(item.title, 64),
        amount = "%.2f".format(item.amount),
        locale = item.locale,
        startsAt = item.startsAt.toString(),
        externalRef = item.externalRef,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCameraEventDigestViews(items: MutableList<PrimaryCameraEventDigest>): MutableList<PrimaryCameraEventDigestView> {
    val views = mutableListOf<PrimaryCameraEventDigestView>()
    for (item in items) {
        views.add(toPrimaryCameraEventDigestView(item))
    }
    return views
}

fun sumPrimaryCameraEventDigestView(view: PrimaryCameraEventDigestView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedAmount + parsedStartsAt.toDouble() + parsedSequence.toDouble()
}
