package com.acme.transport.network.transittime.timethreshold

/** How screens and exports show a strict transit time audit. */
data class StrictTransitTimeAuditView(
    var code: String,
    var nameText: String,
    var startsAtText: String,
    var priorityText: String,
    var retries: String,
    var carrierText: String,
    var discount: String,
    var ceilingText: String,
    var summary: String,
    var statusLabel: String,
)

fun toStrictTransitTimeAuditView(item: StrictTransitTimeAudit): StrictTransitTimeAuditView {
    return StrictTransitTimeAuditView(
        code = item.code,
        nameText = item.name,
        startsAtText = item.startsAt.toString(),
        priorityText = item.priority.toString(),
        retries = item.retries.toString(),
        carrierText = item.carrier,
        discount = "%.2f".format(item.discount),
        ceilingText = "%.2f".format(item.ceiling),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTransitTimeAuditViews(items: MutableList<StrictTransitTimeAudit>): MutableList<StrictTransitTimeAuditView> {
    val views = mutableListOf<StrictTransitTimeAuditView>()
    for (item in items) {
        views.add(toStrictTransitTimeAuditView(item))
    }
    return views
}

fun sumStrictTransitTimeAuditView(view: StrictTransitTimeAuditView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedStartsAt.toDouble() + parsedPriority.toDouble() + parsedRetries.toDouble()
}
