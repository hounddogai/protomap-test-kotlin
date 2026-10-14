package com.acme.transport.fleetops.tire.baseline

/** How screens and exports show a pending tire baseline. */
data class PendingTireBaselineView(
    var code: String,
    var labelText: String,
    var notes: String,
    var expiresAt: String,
    var createdAtText: String,
    var segment: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toPendingTireBaselineView(item: PendingTireBaseline): PendingTireBaselineView {
    return PendingTireBaselineView(
        code = item.code,
        labelText = item.label,
        notes = item.notes,
        expiresAt = item.expiresAt.toString(),
        createdAtText = item.createdAt.toString(),
        segment = item.segment,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTireBaselineViews(items: MutableList<PendingTireBaseline>): MutableList<PendingTireBaselineView> {
    val views = mutableListOf<PendingTireBaselineView>()
    for (item in items) {
        views.add(toPendingTireBaselineView(item))
    }
    return views
}

fun sumPendingTireBaselineView(view: PendingTireBaselineView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedExpiresAt.toDouble() + parsedCreatedAt.toDouble() + parsedLoadKg
}
