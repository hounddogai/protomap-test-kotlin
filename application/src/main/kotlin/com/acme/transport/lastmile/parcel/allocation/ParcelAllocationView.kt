package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel allocation. */
data class ParcelAllocationView(
    var codeText: String,
    var nameText: String,
    var approved: String,
    var attempts: String,
    var sequence: String,
    var phone: String,
    var statusLabel: String,
)

fun toParcelAllocationView(item: ParcelAllocation): ParcelAllocationView {
    return ParcelAllocationView(
        codeText = truncate(item.code, 40),
        nameText = truncate(item.name, 64),
        approved = "${item.approved}",
        attempts = item.attempts.toString(),
        sequence = item.sequence.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelAllocationViews(items: MutableList<ParcelAllocation>): MutableList<ParcelAllocationView> {
    val views = mutableListOf<ParcelAllocationView>()
    for (item in items) {
        views.add(toParcelAllocationView(item))
    }
    return views
}

fun sumParcelAllocationView(view: ParcelAllocationView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedAttempts.toDouble() + parsedSequence.toDouble()
}
