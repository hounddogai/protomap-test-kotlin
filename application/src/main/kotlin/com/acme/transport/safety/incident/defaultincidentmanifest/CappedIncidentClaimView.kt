package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped incident claim. */
data class CappedIncidentClaimView(
    var code: String,
    var labelText: String,
    var description: String,
    var category: String,
    var localeText: String,
    var notes: String,
    var quantity: String,
    var routeCode: String,
    var currency: String,
    var pinned: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toCappedIncidentClaimView(item: CappedIncidentClaim): CappedIncidentClaimView {
    return CappedIncidentClaimView(
        code = item.code,
        labelText = item.label,
        description = truncate(item.description, 120),
        category = item.category,
        localeText = item.locale,
        notes = item.notes,
        quantity = item.quantity.toString(),
        routeCode = truncate(item.routeCode, 80),
        currency = item.currency,
        pinned = "${item.pinned}",
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedIncidentClaimViews(items: MutableList<CappedIncidentClaim>): MutableList<CappedIncidentClaimView> {
    val views = mutableListOf<CappedIncidentClaimView>()
    for (item in items) {
        views.add(toCappedIncidentClaimView(item))
    }
    return views
}

fun sumCappedIncidentClaimView(view: CappedIncidentClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedQuantity.toDouble() + parsedExpiresAt.toDouble()
}
