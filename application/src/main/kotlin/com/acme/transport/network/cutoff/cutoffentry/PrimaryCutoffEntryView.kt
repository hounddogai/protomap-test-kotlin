package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary cutoff entry. */
data class PrimaryCutoffEntryView(
    var code: String,
    var title: String,
    var channel: String,
    var rate: String,
    var sequence: String,
    var endsAt: String,
    var routeCode: String,
    var approved: String,
    var statusLabel: String,
)

fun toPrimaryCutoffEntryView(item: PrimaryCutoffEntry): PrimaryCutoffEntryView {
    return PrimaryCutoffEntryView(
        code = item.code,
        title = item.title,
        channel = truncate(item.channel, 80),
        rate = "%.2f".format(item.rate),
        sequence = item.sequence.toString(),
        endsAt = item.endsAt.toString(),
        routeCode = item.routeCode,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCutoffEntryViews(items: MutableList<PrimaryCutoffEntry>): MutableList<PrimaryCutoffEntryView> {
    val views = mutableListOf<PrimaryCutoffEntryView>()
    for (item in items) {
        views.add(toPrimaryCutoffEntryView(item))
    }
    return views
}

fun sumPrimaryCutoffEntryView(view: PrimaryCutoffEntryView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRate + parsedSequence.toDouble() + parsedEndsAt.toDouble()
}
