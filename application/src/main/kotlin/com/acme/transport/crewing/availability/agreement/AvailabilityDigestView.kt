package com.acme.transport.crewing.availability.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability digest. */
data class AvailabilityDigestView(
    var codeText: String,
    var label: String,
    var dock: String,
    var active: String,
    var expiresAtText: String,
    var source: String,
    var statusLabel: String,
)

fun toAvailabilityDigestView(item: AvailabilityDigest): AvailabilityDigestView {
    return AvailabilityDigestView(
        codeText = truncate(item.code, 80),
        label = item.label,
        dock = truncate(item.dock, 64),
        active = "${item.active}",
        expiresAtText = item.expiresAt.toString(),
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityDigestViews(items: MutableList<AvailabilityDigest>): MutableList<AvailabilityDigestView> {
    val views = mutableListOf<AvailabilityDigestView>()
    for (item in items) {
        views.add(toAvailabilityDigestView(item))
    }
    return views
}

fun sumAvailabilityDigestView(view: AvailabilityDigestView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedExpiresAt.toDouble()
}
