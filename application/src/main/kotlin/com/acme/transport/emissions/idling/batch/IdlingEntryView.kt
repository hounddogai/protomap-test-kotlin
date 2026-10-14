package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling entry. */
data class IdlingEntryView(
    var code: String,
    var name: String,
    var description: String,
    var contactEmailText: String,
    var category: String,
    var windowDays: String,
    var summary: String,
    var statusLabel: String,
)

fun toIdlingEntryView(item: IdlingEntry): IdlingEntryView {
    return IdlingEntryView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 80),
        description = item.description,
        contactEmailText = truncate(item.contactEmail, 80),
        category = item.category,
        windowDays = item.windowDays.toString(),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingEntryViews(items: MutableList<IdlingEntry>): MutableList<IdlingEntryView> {
    val views = mutableListOf<IdlingEntryView>()
    for (item in items) {
        views.add(toIdlingEntryView(item))
    }
    return views
}

fun sumIdlingEntryView(view: IdlingEntryView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
