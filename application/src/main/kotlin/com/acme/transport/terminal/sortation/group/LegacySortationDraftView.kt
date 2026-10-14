package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy sortation draft. */
data class LegacySortationDraftView(
    var codeText: String,
    var label: String,
    var priority: String,
    var dock: String,
    var country: String,
    var statusLabel: String,
)

fun toLegacySortationDraftView(item: LegacySortationDraft): LegacySortationDraftView {
    return LegacySortationDraftView(
        codeText = item.code,
        label = truncate(item.label, 80),
        priority = item.priority.toString(),
        dock = item.dock,
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacySortationDraftViews(items: MutableList<LegacySortationDraft>): MutableList<LegacySortationDraftView> {
    val views = mutableListOf<LegacySortationDraftView>()
    for (item in items) {
        views.add(toLegacySortationDraftView(item))
    }
    return views
}

fun sumLegacySortationDraftView(view: LegacySortationDraftView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
