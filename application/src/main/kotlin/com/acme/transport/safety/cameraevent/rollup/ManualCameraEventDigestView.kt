package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual camera event digest. */
data class ManualCameraEventDigestView(
    var codeText: String,
    var label: String,
    var category: String,
    var retries: String,
    var activeText: String,
    var priority: String,
    var score: String,
    var segment: String,
    var version: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toManualCameraEventDigestView(item: ManualCameraEventDigest): ManualCameraEventDigestView {
    return ManualCameraEventDigestView(
        codeText = item.code,
        label = item.label,
        category = item.category,
        retries = item.retries.toString(),
        activeText = "${item.active}",
        priority = item.priority.toString(),
        score = "%.2f".format(item.score),
        segment = truncate(item.segment, 40),
        version = item.version.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventDigestViews(items: MutableList<ManualCameraEventDigest>): MutableList<ManualCameraEventDigestView> {
    val views = mutableListOf<ManualCameraEventDigestView>()
    for (item in items) {
        views.add(toManualCameraEventDigestView(item))
    }
    return views
}

fun sumManualCameraEventDigestView(view: ManualCameraEventDigestView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRetries.toDouble() + parsedPriority.toDouble() + parsedScore
}
