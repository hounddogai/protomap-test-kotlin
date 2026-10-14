package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched proof of delivery notice. */
data class BatchedProofOfDeliveryNoticeView(
    var code: String,
    var nameText: String,
    var externalRef: String,
    var revision: String,
    var timezone: String,
    var visible: String,
    var summaryText: String,
    var stopCount: String,
    var versionText: String,
    var channel: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toBatchedProofOfDeliveryNoticeView(item: BatchedProofOfDeliveryNotice): BatchedProofOfDeliveryNoticeView {
    return BatchedProofOfDeliveryNoticeView(
        code = item.code,
        nameText = truncate(item.name, 80),
        externalRef = item.externalRef,
        revision = item.revision.toString(),
        timezone = item.timezone,
        visible = "${item.visible}",
        summaryText = item.summary,
        stopCount = item.stopCount.toString(),
        versionText = item.version.toString(),
        channel = item.channel,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedProofOfDeliveryNoticeViews(items: MutableList<BatchedProofOfDeliveryNotice>): MutableList<BatchedProofOfDeliveryNoticeView> {
    val views = mutableListOf<BatchedProofOfDeliveryNoticeView>()
    for (item in items) {
        views.add(toBatchedProofOfDeliveryNoticeView(item))
    }
    return views
}

fun sumBatchedProofOfDeliveryNoticeView(view: BatchedProofOfDeliveryNoticeView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedRevision.toDouble() + parsedStopCount.toDouble() + parsedVersion.toDouble()
}
