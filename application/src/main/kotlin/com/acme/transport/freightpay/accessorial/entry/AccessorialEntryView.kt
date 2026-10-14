package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial entry. */
data class AccessorialEntryView(
    var code: String,
    var title: String,
    var version: String,
    var depot: String,
    var active: String,
    var statusLabel: String,
)

fun toAccessorialEntryView(item: AccessorialEntry): AccessorialEntryView {
    return AccessorialEntryView(
        code = item.code,
        title = item.title,
        version = item.version.toString(),
        depot = truncate(item.depot, 64),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialEntryViews(items: MutableList<AccessorialEntry>): MutableList<AccessorialEntryView> {
    val views = mutableListOf<AccessorialEntryView>()
    for (item in items) {
        views.add(toAccessorialEntryView(item))
    }
    return views
}

fun sumAccessorialEntryView(view: AccessorialEntryView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
