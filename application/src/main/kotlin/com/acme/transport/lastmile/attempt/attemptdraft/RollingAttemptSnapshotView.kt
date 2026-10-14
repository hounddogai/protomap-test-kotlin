package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling attempt snapshot. */
data class RollingAttemptSnapshotView(
    var code: String,
    var name: String,
    var createdAtText: String,
    var reviewerText: String,
    var source: String,
    var etaMinutesText: String,
    var scoreText: String,
    var revision: String,
    var distanceKm: String,
    var driverId: String,
    var statusLabel: String,
)

fun toRollingAttemptSnapshotView(item: RollingAttemptSnapshot): RollingAttemptSnapshotView {
    return RollingAttemptSnapshotView(
        code = item.code,
        name = truncate(item.name, 64),
        createdAtText = item.createdAt.toString(),
        reviewerText = truncate(item.reviewer, 120),
        source = item.source,
        etaMinutesText = item.etaMinutes.toString(),
        scoreText = "%.2f".format(item.score),
        revision = item.revision.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAttemptSnapshotViews(items: MutableList<RollingAttemptSnapshot>): MutableList<RollingAttemptSnapshotView> {
    val views = mutableListOf<RollingAttemptSnapshotView>()
    for (item in items) {
        views.add(toRollingAttemptSnapshotView(item))
    }
    return views
}

fun sumRollingAttemptSnapshotView(view: RollingAttemptSnapshotView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedCreatedAt.toDouble() + parsedEtaMinutes.toDouble() + parsedScore
}
