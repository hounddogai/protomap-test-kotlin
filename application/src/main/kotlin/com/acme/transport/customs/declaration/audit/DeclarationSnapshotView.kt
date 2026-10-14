package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration snapshot. */
data class DeclarationSnapshotView(
    var code: String,
    var titleText: String,
    var routeCodeText: String,
    var capacity: String,
    var graceDays: String,
    var depotText: String,
    var segment: String,
    var approvedText: String,
    var discountText: String,
    var dock: String,
    var statusLabel: String,
)

fun toDeclarationSnapshotView(item: DeclarationSnapshot): DeclarationSnapshotView {
    return DeclarationSnapshotView(
        code = item.code,
        titleText = truncate(item.title, 80),
        routeCodeText = item.routeCode,
        capacity = item.capacity.toString(),
        graceDays = item.graceDays.toString(),
        depotText = item.depot,
        segment = truncate(item.segment, 40),
        approvedText = "${item.approved}",
        discountText = "%.2f".format(item.discount),
        dock = truncate(item.dock, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationSnapshotViews(items: MutableList<DeclarationSnapshot>): MutableList<DeclarationSnapshotView> {
    val views = mutableListOf<DeclarationSnapshotView>()
    for (item in items) {
        views.add(toDeclarationSnapshotView(item))
    }
    return views
}

fun sumDeclarationSnapshotView(view: DeclarationSnapshotView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedCapacity.toDouble() + parsedGraceDays.toDouble() + parsedDiscount
}
