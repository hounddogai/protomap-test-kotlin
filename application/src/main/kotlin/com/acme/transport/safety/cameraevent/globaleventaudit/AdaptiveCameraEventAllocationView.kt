package com.acme.transport.safety.cameraevent.globaleventaudit

/** How screens and exports show a adaptive camera event allocation. */
data class AdaptiveCameraEventAllocationView(
    var code: String,
    var labelText: String,
    var durationMinutes: String,
    var retries: String,
    var loadKg: String,
    var versionText: String,
    var statusLabel: String,
)

fun toAdaptiveCameraEventAllocationView(item: AdaptiveCameraEventAllocation): AdaptiveCameraEventAllocationView {
    return AdaptiveCameraEventAllocationView(
        code = item.code,
        labelText = item.label,
        durationMinutes = item.durationMinutes.toString(),
        retries = item.retries.toString(),
        loadKg = "%.2f".format(item.loadKg),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCameraEventAllocationViews(items: MutableList<AdaptiveCameraEventAllocation>): MutableList<AdaptiveCameraEventAllocationView> {
    val views = mutableListOf<AdaptiveCameraEventAllocationView>()
    for (item in items) {
        views.add(toAdaptiveCameraEventAllocationView(item))
    }
    return views
}

fun sumAdaptiveCameraEventAllocationView(view: AdaptiveCameraEventAllocationView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedDurationMinutes.toDouble() + parsedRetries.toDouble() + parsedLoadKg
}
