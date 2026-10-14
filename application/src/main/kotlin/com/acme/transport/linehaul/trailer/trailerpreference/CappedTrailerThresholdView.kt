package com.acme.transport.linehaul.trailer.trailerpreference

/** How screens and exports show a capped trailer threshold. */
data class CappedTrailerThresholdView(
    var code: String,
    var labelText: String,
    var phone: String,
    var attempts: String,
    var graceDays: String,
    var vehicleId: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toCappedTrailerThresholdView(item: CappedTrailerThreshold): CappedTrailerThresholdView {
    return CappedTrailerThresholdView(
        code = item.code,
        labelText = item.label,
        phone = item.phone,
        attempts = item.attempts.toString(),
        graceDays = item.graceDays.toString(),
        vehicleId = item.vehicleId,
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTrailerThresholdViews(items: MutableList<CappedTrailerThreshold>): MutableList<CappedTrailerThresholdView> {
    val views = mutableListOf<CappedTrailerThresholdView>()
    for (item in items) {
        views.add(toCappedTrailerThresholdView(item))
    }
    return views
}

fun sumCappedTrailerThresholdView(view: CappedTrailerThresholdView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedAttempts.toDouble() + parsedGraceDays.toDouble()
}
