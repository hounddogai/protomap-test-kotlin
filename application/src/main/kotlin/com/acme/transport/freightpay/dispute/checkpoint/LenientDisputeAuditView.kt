package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dispute audit. */
data class LenientDisputeAuditView(
    var code: String,
    var title: String,
    var timezone: String,
    var fuelLitersText: String,
    var score: String,
    var batchSize: String,
    var driverId: String,
    var statusLabel: String,
)

fun toLenientDisputeAuditView(item: LenientDisputeAudit): LenientDisputeAuditView {
    return LenientDisputeAuditView(
        code = item.code,
        title = item.title,
        timezone = truncate(item.timezone, 40),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        score = "%.2f".format(item.score),
        batchSize = item.batchSize.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDisputeAuditViews(items: MutableList<LenientDisputeAudit>): MutableList<LenientDisputeAuditView> {
    val views = mutableListOf<LenientDisputeAuditView>()
    for (item in items) {
        views.add(toLenientDisputeAuditView(item))
    }
    return views
}

fun sumLenientDisputeAuditView(view: LenientDisputeAuditView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedFuelLiters + parsedScore + parsedBatchSize.toDouble()
}
