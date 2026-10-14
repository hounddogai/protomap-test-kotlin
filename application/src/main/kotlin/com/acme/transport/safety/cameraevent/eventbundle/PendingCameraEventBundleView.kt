package com.acme.transport.safety.cameraevent.eventbundle

/** How screens and exports show a pending camera event bundle. */
data class PendingCameraEventBundleView(
    var code: String,
    var title: String,
    var routeCode: String,
    var revision: String,
    var graceDays: String,
    var source: String,
    var version: String,
    var statusLabel: String,
)

fun toPendingCameraEventBundleView(item: PendingCameraEventBundle): PendingCameraEventBundleView {
    return PendingCameraEventBundleView(
        code = item.code,
        title = item.title,
        routeCode = item.routeCode,
        revision = item.revision.toString(),
        graceDays = item.graceDays.toString(),
        source = item.source,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCameraEventBundleViews(items: MutableList<PendingCameraEventBundle>): MutableList<PendingCameraEventBundleView> {
    val views = mutableListOf<PendingCameraEventBundleView>()
    for (item in items) {
        views.add(toPendingCameraEventBundleView(item))
    }
    return views
}

fun sumPendingCameraEventBundleView(view: PendingCameraEventBundleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedVersion = view.version.toInt()
    return parsedRevision.toDouble() + parsedGraceDays.toDouble() + parsedVersion.toDouble()
}
