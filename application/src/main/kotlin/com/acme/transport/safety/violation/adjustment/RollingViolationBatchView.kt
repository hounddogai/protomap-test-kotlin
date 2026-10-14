package com.acme.transport.safety.violation.adjustment

/** How screens and exports show a rolling violation batch. */
data class RollingViolationBatchView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var endsAt: String,
    var localeText: String,
    var startsAt: String,
    var score: String,
    var discount: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toRollingViolationBatchView(item: RollingViolationBatch): RollingViolationBatchView {
    return RollingViolationBatchView(
        code = item.code,
        label = item.label,
        routeCodeText = item.routeCode,
        endsAt = item.endsAt.toString(),
        localeText = item.locale,
        startsAt = item.startsAt.toString(),
        score = "%.2f".format(item.score),
        discount = "%.2f".format(item.discount),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingViolationBatchViews(items: MutableList<RollingViolationBatch>): MutableList<RollingViolationBatchView> {
    val views = mutableListOf<RollingViolationBatchView>()
    for (item in items) {
        views.add(toRollingViolationBatchView(item))
    }
    return views
}

fun sumRollingViolationBatchView(view: RollingViolationBatchView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedEndsAt.toDouble() + parsedStartsAt.toDouble() + parsedScore
}
