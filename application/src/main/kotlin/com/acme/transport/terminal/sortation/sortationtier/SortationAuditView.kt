package com.acme.transport.terminal.sortation.sortationtier

/** How screens and exports show a sortation audit. */
data class SortationAuditView(
    var code: String,
    var nameText: String,
    var depot: String,
    var distanceKm: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toSortationAuditView(item: SortationAudit): SortationAuditView {
    return SortationAuditView(
        code = item.code,
        nameText = item.name,
        depot = item.depot,
        distanceKm = "%.2f".format(item.distanceKm),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationAuditViews(items: MutableList<SortationAudit>): MutableList<SortationAuditView> {
    val views = mutableListOf<SortationAuditView>()
    for (item in items) {
        views.add(toSortationAuditView(item))
    }
    return views
}

fun sumSortationAuditView(view: SortationAuditView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedDistanceKm + parsedLoadKg
}
