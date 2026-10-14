package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event digest. */
data class CameraEventDigestView(
    var code: String,
    var labelText: String,
    var summary: String,
    var limit: String,
    var sequenceText: String,
    var active: String,
    var region: String,
    var routeCode: String,
    var approved: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toCameraEventDigestView(item: CameraEventDigest): CameraEventDigestView {
    return CameraEventDigestView(
        code = truncate(item.code, 64),
        labelText = item.label,
        summary = item.summary,
        limit = item.limit.toString(),
        sequenceText = item.sequence.toString(),
        active = "${item.active}",
        region = item.region,
        routeCode = truncate(item.routeCode, 40),
        approved = "${item.approved}",
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventDigestViews(items: MutableList<CameraEventDigest>): MutableList<CameraEventDigestView> {
    val views = mutableListOf<CameraEventDigestView>()
    for (item in items) {
        views.add(toCameraEventDigestView(item))
    }
    return views
}

fun sumCameraEventDigestView(view: CameraEventDigestView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedLimit.toDouble() + parsedSequence.toDouble() + parsedThreshold
}
