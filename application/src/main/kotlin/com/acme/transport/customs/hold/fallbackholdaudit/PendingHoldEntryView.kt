package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hold entry. */
data class PendingHoldEntryView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var contactEmail: String,
    var limit: String,
    var sequence: String,
    var phone: String,
    var quantity: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toPendingHoldEntryView(item: PendingHoldEntry): PendingHoldEntryView {
    return PendingHoldEntryView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 64),
        updatedAt = item.updatedAt.toString(),
        contactEmail = item.contactEmail,
        limit = item.limit.toString(),
        sequence = item.sequence.toString(),
        phone = truncate(item.phone, 40),
        quantity = item.quantity.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHoldEntryViews(items: MutableList<PendingHoldEntry>): MutableList<PendingHoldEntryView> {
    val views = mutableListOf<PendingHoldEntryView>()
    for (item in items) {
        views.add(toPendingHoldEntryView(item))
    }
    return views
}

fun sumPendingHoldEntryView(view: PendingHoldEntryView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedUpdatedAt.toDouble() + parsedLimit.toDouble() + parsedSequence.toDouble()
}
