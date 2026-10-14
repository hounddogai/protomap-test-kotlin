package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched carbon report policy. */
data class BatchedCarbonReportPolicyView(
    var code: String,
    var labelText: String,
    var ownerIdText: String,
    var revisionText: String,
    var retries: String,
    var activeText: String,
    var capacityText: String,
    var marginText: String,
    var statusLabel: String,
)

fun toBatchedCarbonReportPolicyView(item: BatchedCarbonReportPolicy): BatchedCarbonReportPolicyView {
    return BatchedCarbonReportPolicyView(
        code = item.code,
        labelText = truncate(item.label, 80),
        ownerIdText = item.ownerId,
        revisionText = item.revision.toString(),
        retries = item.retries.toString(),
        activeText = "${item.active}",
        capacityText = item.capacity.toString(),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCarbonReportPolicyViews(items: MutableList<BatchedCarbonReportPolicy>): MutableList<BatchedCarbonReportPolicyView> {
    val views = mutableListOf<BatchedCarbonReportPolicyView>()
    for (item in items) {
        views.add(toBatchedCarbonReportPolicyView(item))
    }
    return views
}

fun sumBatchedCarbonReportPolicyView(view: BatchedCarbonReportPolicyView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedRevision.toDouble() + parsedRetries.toDouble() + parsedCapacity.toDouble()
}
