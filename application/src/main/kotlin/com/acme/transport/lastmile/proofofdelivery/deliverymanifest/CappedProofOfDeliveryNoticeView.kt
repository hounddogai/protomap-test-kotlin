package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped proof of delivery notice. */
data class CappedProofOfDeliveryNoticeView(
    var code: String,
    var name: String,
    var version: String,
    var discount: String,
    var stopCount: String,
    var priorityText: String,
    var revisionText: String,
    var batchSize: String,
    var etaMinutesText: String,
    var visible: String,
    var amount: String,
    var statusLabel: String,
)

fun toCappedProofOfDeliveryNoticeView(item: CappedProofOfDeliveryNotice): CappedProofOfDeliveryNoticeView {
    return CappedProofOfDeliveryNoticeView(
        code = item.code,
        name = truncate(item.name, 64),
        version = item.version.toString(),
        discount = "%.2f".format(item.discount),
        stopCount = item.stopCount.toString(),
        priorityText = item.priority.toString(),
        revisionText = item.revision.toString(),
        batchSize = item.batchSize.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        visible = "${item.visible}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedProofOfDeliveryNoticeViews(items: MutableList<CappedProofOfDeliveryNotice>): MutableList<CappedProofOfDeliveryNoticeView> {
    val views = mutableListOf<CappedProofOfDeliveryNoticeView>()
    for (item in items) {
        views.add(toCappedProofOfDeliveryNoticeView(item))
    }
    return views
}

fun sumCappedProofOfDeliveryNoticeView(view: CappedProofOfDeliveryNoticeView): Double {
    val parsedVersion = view.version.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedVersion.toDouble() + parsedDiscount + parsedStopCount.toDouble()
}
