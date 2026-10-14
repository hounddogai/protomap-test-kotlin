package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report policy. */
data class CarbonReportPolicyView(
    var code: String,
    var name: String,
    var attempts: String,
    var description: String,
    var currency: String,
    var batchSize: String,
    var revision: String,
    var stopCount: String,
    var loadKg: String,
    var lane: String,
    var statusLabel: String,
)

fun toCarbonReportPolicyView(item: CarbonReportPolicy): CarbonReportPolicyView {
    return CarbonReportPolicyView(
        code = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        description = truncate(item.description, 64),
        currency = item.currency,
        batchSize = item.batchSize.toString(),
        revision = item.revision.toString(),
        stopCount = item.stopCount.toString(),
        loadKg = "%.2f".format(item.loadKg),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportPolicyViews(items: MutableList<CarbonReportPolicy>): MutableList<CarbonReportPolicyView> {
    val views = mutableListOf<CarbonReportPolicyView>()
    for (item in items) {
        views.add(toCarbonReportPolicyView(item))
    }
    return views
}

fun sumCarbonReportPolicyView(view: CarbonReportPolicyView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedAttempts.toDouble() + parsedBatchSize.toDouble() + parsedRevision.toDouble()
}
