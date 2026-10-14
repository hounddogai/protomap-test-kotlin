package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation draft. */
data class ViolationDraftView(
    var code: String,
    var labelText: String,
    var notes: String,
    var expiresAtText: String,
    var contactEmail: String,
    var trailerIdText: String,
    var pinned: String,
    var ceiling: String,
    var region: String,
    var statusLabel: String,
)

fun toViolationDraftView(item: ViolationDraft): ViolationDraftView {
    return ViolationDraftView(
        code = item.code,
        labelText = item.label,
        notes = item.notes,
        expiresAtText = item.expiresAt.toString(),
        contactEmail = item.contactEmail,
        trailerIdText = truncate(item.trailerId, 120),
        pinned = "${item.pinned}",
        ceiling = "%.2f".format(item.ceiling),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationDraftViews(items: MutableList<ViolationDraft>): MutableList<ViolationDraftView> {
    val views = mutableListOf<ViolationDraftView>()
    for (item in items) {
        views.add(toViolationDraftView(item))
    }
    return views
}

fun sumViolationDraftView(view: ViolationDraftView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedExpiresAt.toDouble() + parsedCeiling
}
