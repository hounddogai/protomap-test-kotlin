package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cross dock snapshot. */
data class DefaultCrossDockSnapshotView(
    var code: String,
    var nameText: String,
    var retries: String,
    var lane: String,
    var expiresAt: String,
    var shiftCode: String,
    var notesText: String,
    var budgetText: String,
    var createdAt: String,
    var sequence: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toDefaultCrossDockSnapshotView(item: DefaultCrossDockSnapshot): DefaultCrossDockSnapshotView {
    return DefaultCrossDockSnapshotView(
        code = truncate(item.code, 120),
        nameText = item.name,
        retries = item.retries.toString(),
        lane = truncate(item.lane, 40),
        expiresAt = item.expiresAt.toString(),
        shiftCode = item.shiftCode,
        notesText = item.notes,
        budgetText = "%.2f".format(item.budget),
        createdAt = item.createdAt.toString(),
        sequence = item.sequence.toString(),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCrossDockSnapshotViews(items: MutableList<DefaultCrossDockSnapshot>): MutableList<DefaultCrossDockSnapshotView> {
    val views = mutableListOf<DefaultCrossDockSnapshotView>()
    for (item in items) {
        views.add(toDefaultCrossDockSnapshotView(item))
    }
    return views
}

fun sumDefaultCrossDockSnapshotView(view: DefaultCrossDockSnapshotView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedRetries.toDouble() + parsedExpiresAt.toDouble() + parsedBudget
}
