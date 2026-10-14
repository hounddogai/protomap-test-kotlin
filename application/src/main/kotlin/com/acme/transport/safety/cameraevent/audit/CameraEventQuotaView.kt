package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event quota. */
data class CameraEventQuotaView(
    var code: String,
    var name: String,
    var externalRefText: String,
    var sourceText: String,
    var reviewer: String,
    var lane: String,
    var phone: String,
    var score: String,
    var statusLabel: String,
)

fun toCameraEventQuotaView(item: CameraEventQuota): CameraEventQuotaView {
    return CameraEventQuotaView(
        code = item.code,
        name = item.name,
        externalRefText = truncate(item.externalRef, 64),
        sourceText = truncate(item.source, 120),
        reviewer = truncate(item.reviewer, 40),
        lane = item.lane,
        phone = truncate(item.phone, 64),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventQuotaViews(items: MutableList<CameraEventQuota>): MutableList<CameraEventQuotaView> {
    val views = mutableListOf<CameraEventQuotaView>()
    for (item in items) {
        views.add(toCameraEventQuotaView(item))
    }
    return views
}

fun sumCameraEventQuotaView(view: CameraEventQuotaView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
