package com.acme.transport.crewing.restbreak.assignment

/** How screens and exports show a rest break override. */
data class RestBreakOverrideView(
    var code: String,
    var name: String,
    var regionText: String,
    var startsAt: String,
    var carrier: String,
    var revision: String,
    var weight: String,
    var score: String,
    var externalRef: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toRestBreakOverrideView(item: RestBreakOverride): RestBreakOverrideView {
    return RestBreakOverrideView(
        code = item.code,
        name = item.name,
        regionText = item.region,
        startsAt = item.startsAt.toString(),
        carrier = item.carrier,
        revision = item.revision.toString(),
        weight = "%.2f".format(item.weight),
        score = "%.2f".format(item.score),
        externalRef = item.externalRef,
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakOverrideViews(items: MutableList<RestBreakOverride>): MutableList<RestBreakOverrideView> {
    val views = mutableListOf<RestBreakOverrideView>()
    for (item in items) {
        views.add(toRestBreakOverrideView(item))
    }
    return views
}

fun sumRestBreakOverrideView(view: RestBreakOverrideView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedStartsAt.toDouble() + parsedRevision.toDouble() + parsedWeight
}
