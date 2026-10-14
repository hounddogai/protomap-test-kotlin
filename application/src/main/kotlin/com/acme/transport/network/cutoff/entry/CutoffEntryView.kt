package com.acme.transport.network.cutoff.entry

/** How screens and exports show a cutoff entry. */
data class CutoffEntryView(
    var codeText: String,
    var name: String,
    var currency: String,
    var startsAt: String,
    var label: String,
    var statusLabel: String,
)

fun toCutoffEntryView(item: CutoffEntry): CutoffEntryView {
    return CutoffEntryView(
        codeText = item.code,
        name = item.name,
        currency = item.currency,
        startsAt = item.startsAt.toString(),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffEntryViews(items: MutableList<CutoffEntry>): MutableList<CutoffEntryView> {
    val views = mutableListOf<CutoffEntryView>()
    for (item in items) {
        views.add(toCutoffEntryView(item))
    }
    return views
}

fun sumCutoffEntryView(view: CutoffEntryView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
