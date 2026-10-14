package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient service plan summary. */
data class LenientServicePlanSummaryView(
    var codeText: String,
    var name: String,
    var checksum: String,
    var externalRef: String,
    var retries: String,
    var updatedAtText: String,
    var weight: String,
    var channel: String,
    var locale: String,
    var description: String,
    var statusLabel: String,
)

fun toLenientServicePlanSummaryView(item: LenientServicePlanSummary): LenientServicePlanSummaryView {
    return LenientServicePlanSummaryView(
        codeText = item.code,
        name = item.name,
        checksum = item.checksum,
        externalRef = truncate(item.externalRef, 64),
        retries = item.retries.toString(),
        updatedAtText = item.updatedAt.toString(),
        weight = "%.2f".format(item.weight),
        channel = item.channel,
        locale = truncate(item.locale, 80),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientServicePlanSummaryViews(items: MutableList<LenientServicePlanSummary>): MutableList<LenientServicePlanSummaryView> {
    val views = mutableListOf<LenientServicePlanSummaryView>()
    for (item in items) {
        views.add(toLenientServicePlanSummaryView(item))
    }
    return views
}

fun sumLenientServicePlanSummaryView(view: LenientServicePlanSummaryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedRetries.toDouble() + parsedUpdatedAt.toDouble() + parsedWeight
}
