package com.acme.transport.freightpay.freightinvoice.review

/** How screens and exports show a batched freight invoice review. */
data class BatchedFreightInvoiceReviewView(
    var code: String,
    var title: String,
    var routeCodeText: String,
    var revision: String,
    var sourceText: String,
    var countryText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toBatchedFreightInvoiceReviewView(item: BatchedFreightInvoiceReview): BatchedFreightInvoiceReviewView {
    return BatchedFreightInvoiceReviewView(
        code = item.code,
        title = item.title,
        routeCodeText = item.routeCode,
        revision = item.revision.toString(),
        sourceText = item.source,
        countryText = item.country,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedFreightInvoiceReviewViews(items: MutableList<BatchedFreightInvoiceReview>): MutableList<BatchedFreightInvoiceReviewView> {
    val views = mutableListOf<BatchedFreightInvoiceReviewView>()
    for (item in items) {
        views.add(toBatchedFreightInvoiceReviewView(item))
    }
    return views
}

fun sumBatchedFreightInvoiceReviewView(view: BatchedFreightInvoiceReviewView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedRevision.toDouble() + parsedSequence.toDouble()
}
