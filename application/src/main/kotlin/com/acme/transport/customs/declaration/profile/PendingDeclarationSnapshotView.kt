package com.acme.transport.customs.declaration.profile

/** How screens and exports show a pending declaration snapshot. */
data class PendingDeclarationSnapshotView(
    var code: String,
    var titleText: String,
    var driverId: String,
    var loadKgText: String,
    var timezone: String,
    var cost: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toPendingDeclarationSnapshotView(item: PendingDeclarationSnapshot): PendingDeclarationSnapshotView {
    return PendingDeclarationSnapshotView(
        code = item.code,
        titleText = item.title,
        driverId = item.driverId,
        loadKgText = "%.2f".format(item.loadKg),
        timezone = item.timezone,
        cost = "%.2f".format(item.cost),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDeclarationSnapshotViews(items: MutableList<PendingDeclarationSnapshot>): MutableList<PendingDeclarationSnapshotView> {
    val views = mutableListOf<PendingDeclarationSnapshotView>()
    for (item in items) {
        views.add(toPendingDeclarationSnapshotView(item))
    }
    return views
}

fun sumPendingDeclarationSnapshotView(view: PendingDeclarationSnapshotView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedLoadKg + parsedCost
}
