package com.acme.transport.safety.cameraevent.snapshot

/** How screens and exports show a rolling camera event snapshot. */
data class RollingCameraEventSnapshotView(
    var code: String,
    var title: String,
    var region: String,
    var ratioText: String,
    var weight: String,
    var statusLabel: String,
)

fun toRollingCameraEventSnapshotView(item: RollingCameraEventSnapshot): RollingCameraEventSnapshotView {
    return RollingCameraEventSnapshotView(
        code = item.code,
        title = item.title,
        region = item.region,
        ratioText = "%.2f".format(item.ratio),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCameraEventSnapshotViews(items: MutableList<RollingCameraEventSnapshot>): MutableList<RollingCameraEventSnapshotView> {
    val views = mutableListOf<RollingCameraEventSnapshotView>()
    for (item in items) {
        views.add(toRollingCameraEventSnapshotView(item))
    }
    return views
}

fun sumRollingCameraEventSnapshotView(view: RollingCameraEventSnapshotView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedRatio + parsedWeight
}
