package com.acme.transport.terminal.traileryard.yardquota

/** How screens and exports show a batched trailer yard quota. */
data class BatchedTrailerYardQuotaView(
    var code: String,
    var title: String,
    var odometer: String,
    var contactEmail: String,
    var pinned: String,
    var margin: String,
    var region: String,
    var routeCodeText: String,
    var endsAt: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toBatchedTrailerYardQuotaView(item: BatchedTrailerYardQuota): BatchedTrailerYardQuotaView {
    return BatchedTrailerYardQuotaView(
        code = item.code,
        title = item.title,
        odometer = item.odometer.toString(),
        contactEmail = item.contactEmail,
        pinned = "${item.pinned}",
        margin = "%.2f".format(item.margin),
        region = item.region,
        routeCodeText = item.routeCode,
        endsAt = item.endsAt.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTrailerYardQuotaViews(items: MutableList<BatchedTrailerYardQuota>): MutableList<BatchedTrailerYardQuotaView> {
    val views = mutableListOf<BatchedTrailerYardQuotaView>()
    for (item in items) {
        views.add(toBatchedTrailerYardQuotaView(item))
    }
    return views
}

fun sumBatchedTrailerYardQuotaView(view: BatchedTrailerYardQuotaView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedOdometer.toDouble() + parsedMargin + parsedEndsAt.toDouble()
}
