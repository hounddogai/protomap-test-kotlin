package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered tire entry. */
data class TieredTireEntryView(
    var code: String,
    var title: String,
    var sequence: String,
    var palletsText: String,
    var shiftCodeText: String,
    var priority: String,
    var expiresAt: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toTieredTireEntryView(item: TieredTireEntry): TieredTireEntryView {
    return TieredTireEntryView(
        code = item.code,
        title = item.title,
        sequence = item.sequence.toString(),
        palletsText = item.pallets.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        priority = item.priority.toString(),
        expiresAt = item.expiresAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTireEntryViews(items: MutableList<TieredTireEntry>): MutableList<TieredTireEntryView> {
    val views = mutableListOf<TieredTireEntryView>()
    for (item in items) {
        views.add(toTieredTireEntryView(item))
    }
    return views
}

fun sumTieredTireEntryView(view: TieredTireEntryView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedSequence.toDouble() + parsedPallets.toDouble() + parsedPriority.toDouble()
}
