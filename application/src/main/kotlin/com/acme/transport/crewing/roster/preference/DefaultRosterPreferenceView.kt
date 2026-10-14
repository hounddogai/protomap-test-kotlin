package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default roster preference. */
data class DefaultRosterPreferenceView(
    var codeText: String,
    var title: String,
    var phone: String,
    var category: String,
    var contactEmail: String,
    var carrier: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toDefaultRosterPreferenceView(item: DefaultRosterPreference): DefaultRosterPreferenceView {
    return DefaultRosterPreferenceView(
        codeText = item.code,
        title = truncate(item.title, 120),
        phone = item.phone,
        category = item.category,
        contactEmail = item.contactEmail,
        carrier = truncate(item.carrier, 64),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRosterPreferenceViews(items: MutableList<DefaultRosterPreference>): MutableList<DefaultRosterPreferenceView> {
    val views = mutableListOf<DefaultRosterPreferenceView>()
    for (item in items) {
        views.add(toDefaultRosterPreferenceView(item))
    }
    return views
}

fun sumDefaultRosterPreferenceView(view: DefaultRosterPreferenceView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}
