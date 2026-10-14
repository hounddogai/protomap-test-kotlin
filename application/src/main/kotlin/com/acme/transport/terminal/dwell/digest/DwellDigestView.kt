package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell digest. */
data class DwellDigestView(
    var code: String,
    var label: String,
    var attempts: String,
    var loadKgText: String,
    var dockText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toDwellDigestView(item: DwellDigest): DwellDigestView {
    return DwellDigestView(
        code = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        dockText = truncate(item.dock, 64),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellDigestViews(items: MutableList<DwellDigest>): MutableList<DwellDigestView> {
    val views = mutableListOf<DwellDigestView>()
    for (item in items) {
        views.add(toDwellDigestView(item))
    }
    return views
}

fun sumDwellDigestView(view: DwellDigestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedAttempts.toDouble() + parsedLoadKg + parsedExpiresAt.toDouble()
}
