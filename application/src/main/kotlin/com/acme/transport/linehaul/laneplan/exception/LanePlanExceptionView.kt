package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan exception. */
data class LanePlanExceptionView(
    var code: String,
    var titleText: String,
    var batchSize: String,
    var score: String,
    var routeCodeText: String,
    var driverId: String,
    var createdAt: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toLanePlanExceptionView(item: LanePlanException): LanePlanExceptionView {
    return LanePlanExceptionView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 80),
        batchSize = item.batchSize.toString(),
        score = "%.2f".format(item.score),
        routeCodeText = item.routeCode,
        driverId = item.driverId,
        createdAt = item.createdAt.toString(),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanExceptionViews(items: MutableList<LanePlanException>): MutableList<LanePlanExceptionView> {
    val views = mutableListOf<LanePlanExceptionView>()
    for (item in items) {
        views.add(toLanePlanExceptionView(item))
    }
    return views
}

fun sumLanePlanExceptionView(view: LanePlanExceptionView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedScore = view.score.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedBatchSize.toDouble() + parsedScore + parsedCreatedAt.toDouble()
}
