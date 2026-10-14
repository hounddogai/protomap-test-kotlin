package com.acme.transport.terminal.traileryard.tier

/** How screens and exports show a batched trailer yard audit. */
data class BatchedTrailerYardAuditView(
    var code: String,
    var label: String,
    var approved: String,
    var batchSize: String,
    var shiftCodeText: String,
    var quantity: String,
    var retries: String,
    var countryText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toBatchedTrailerYardAuditView(item: BatchedTrailerYardAudit): BatchedTrailerYardAuditView {
    return BatchedTrailerYardAuditView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        batchSize = item.batchSize.toString(),
        shiftCodeText = item.shiftCode,
        quantity = item.quantity.toString(),
        retries = item.retries.toString(),
        countryText = item.country,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTrailerYardAuditViews(items: MutableList<BatchedTrailerYardAudit>): MutableList<BatchedTrailerYardAuditView> {
    val views = mutableListOf<BatchedTrailerYardAuditView>()
    for (item in items) {
        views.add(toBatchedTrailerYardAuditView(item))
    }
    return views
}

fun sumBatchedTrailerYardAuditView(view: BatchedTrailerYardAuditView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedBatchSize.toDouble() + parsedQuantity.toDouble() + parsedRetries.toDouble()
}
