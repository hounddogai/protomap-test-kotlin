package com.acme.transport.crewing.availability.digest

/** How screens and exports show a batched availability digest. */
data class BatchedAvailabilityDigestView(
    var code: String,
    var labelText: String,
    var retriesText: String,
    var odometer: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toBatchedAvailabilityDigestView(item: BatchedAvailabilityDigest): BatchedAvailabilityDigestView {
    return BatchedAvailabilityDigestView(
        code = item.code,
        labelText = item.label,
        retriesText = item.retries.toString(),
        odometer = item.odometer.toString(),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAvailabilityDigestViews(items: MutableList<BatchedAvailabilityDigest>): MutableList<BatchedAvailabilityDigestView> {
    val views = mutableListOf<BatchedAvailabilityDigestView>()
    for (item in items) {
        views.add(toBatchedAvailabilityDigestView(item))
    }
    return views
}

fun sumBatchedAvailabilityDigestView(view: BatchedAvailabilityDigestView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedRetries.toDouble() + parsedOdometer.toDouble()
}
