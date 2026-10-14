package com.acme.transport.safety.cameraevent.eventslot

/** How screens and exports show a fallback camera event snapshot. */
data class FallbackCameraEventSnapshotView(
    var codeText: String,
    var labelText: String,
    var floorValue: String,
    var limitText: String,
    var timezone: String,
    var region: String,
    var statusLabel: String,
)

fun toFallbackCameraEventSnapshotView(item: FallbackCameraEventSnapshot): FallbackCameraEventSnapshotView {
    return FallbackCameraEventSnapshotView(
        codeText = item.code,
        labelText = item.label,
        floorValue = "%.2f".format(item.floorValue),
        limitText = item.limit.toString(),
        timezone = item.timezone,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCameraEventSnapshotViews(items: MutableList<FallbackCameraEventSnapshot>): MutableList<FallbackCameraEventSnapshotView> {
    val views = mutableListOf<FallbackCameraEventSnapshotView>()
    for (item in items) {
        views.add(toFallbackCameraEventSnapshotView(item))
    }
    return views
}

fun sumFallbackCameraEventSnapshotView(view: FallbackCameraEventSnapshotView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedFloorValue + parsedLimit.toDouble()
}
