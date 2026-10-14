package com.acme.transport.freightpay.dispute.digest

/** How screens and exports show a default dispute snapshot. */
data class DefaultDisputeSnapshotView(
    var codeText: String,
    var labelText: String,
    var driverId: String,
    var windowDays: String,
    var createdAt: String,
    var descriptionText: String,
    var rateText: String,
    var name: String,
    var statusLabel: String,
)

fun toDefaultDisputeSnapshotView(item: DefaultDisputeSnapshot): DefaultDisputeSnapshotView {
    return DefaultDisputeSnapshotView(
        codeText = item.code,
        labelText = item.label,
        driverId = item.driverId,
        windowDays = item.windowDays.toString(),
        createdAt = item.createdAt.toString(),
        descriptionText = item.description,
        rateText = "%.2f".format(item.rate),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDisputeSnapshotViews(items: MutableList<DefaultDisputeSnapshot>): MutableList<DefaultDisputeSnapshotView> {
    val views = mutableListOf<DefaultDisputeSnapshotView>()
    for (item in items) {
        views.add(toDefaultDisputeSnapshotView(item))
    }
    return views
}

fun sumDefaultDisputeSnapshotView(view: DefaultDisputeSnapshotView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedWindowDays.toDouble() + parsedCreatedAt.toDouble() + parsedRate
}
