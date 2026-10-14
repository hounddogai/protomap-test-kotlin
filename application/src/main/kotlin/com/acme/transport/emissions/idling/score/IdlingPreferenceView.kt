package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling preference. */
data class IdlingPreferenceView(
    var code: String,
    var title: String,
    var locale: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toIdlingPreferenceView(item: IdlingPreference): IdlingPreferenceView {
    return IdlingPreferenceView(
        code = item.code,
        title = item.title,
        locale = truncate(item.locale, 40),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingPreferenceViews(items: MutableList<IdlingPreference>): MutableList<IdlingPreferenceView> {
    val views = mutableListOf<IdlingPreferenceView>()
    for (item in items) {
        views.add(toIdlingPreferenceView(item))
    }
    return views
}

fun sumIdlingPreferenceView(view: IdlingPreferenceView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
