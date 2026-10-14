package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling defect snapshot. */
data class RollingDefectSnapshotView(
    var code: String,
    var label: String,
    var limitText: String,
    var attempts: String,
    var score: String,
    var expiresAt: String,
    var externalRef: String,
    var carrierText: String,
    var revision: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toRollingDefectSnapshotView(item: RollingDefectSnapshot): RollingDefectSnapshotView {
    return RollingDefectSnapshotView(
        code = item.code,
        label = truncate(item.label, 80),
        limitText = item.limit.toString(),
        attempts = item.attempts.toString(),
        score = "%.2f".format(item.score),
        expiresAt = item.expiresAt.toString(),
        externalRef = truncate(item.externalRef, 40),
        carrierText = item.carrier,
        revision = item.revision.toString(),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDefectSnapshotViews(items: MutableList<RollingDefectSnapshot>): MutableList<RollingDefectSnapshotView> {
    val views = mutableListOf<RollingDefectSnapshotView>()
    for (item in items) {
        views.add(toRollingDefectSnapshotView(item))
    }
    return views
}

fun sumRollingDefectSnapshotView(view: RollingDefectSnapshotView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedScore = view.score.toDouble()
    return parsedLimit.toDouble() + parsedAttempts.toDouble() + parsedScore
}
