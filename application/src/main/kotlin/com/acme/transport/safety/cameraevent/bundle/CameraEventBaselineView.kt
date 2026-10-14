package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event baseline. */
data class CameraEventBaselineView(
    var code: String,
    var labelText: String,
    var pinned: String,
    var phone: String,
    var name: String,
    var limit: String,
    var reviewerText: String,
    var updatedAtText: String,
    var sourceText: String,
    var revision: String,
    var country: String,
    var statusLabel: String,
)

fun toCameraEventBaselineView(item: CameraEventBaseline): CameraEventBaselineView {
    return CameraEventBaselineView(
        code = item.code,
        labelText = truncate(item.label, 80),
        pinned = "${item.pinned}",
        phone = item.phone,
        name = item.name,
        limit = item.limit.toString(),
        reviewerText = truncate(item.reviewer, 120),
        updatedAtText = item.updatedAt.toString(),
        sourceText = item.source,
        revision = item.revision.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventBaselineViews(items: MutableList<CameraEventBaseline>): MutableList<CameraEventBaselineView> {
    val views = mutableListOf<CameraEventBaselineView>()
    for (item in items) {
        views.add(toCameraEventBaselineView(item))
    }
    return views
}

fun sumCameraEventBaselineView(view: CameraEventBaselineView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedLimit.toDouble() + parsedUpdatedAt.toDouble() + parsedRevision.toDouble()
}
