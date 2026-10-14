package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual freight invoice review. */
data class ManualFreightInvoiceReviewView(
    var codeText: String,
    var name: String,
    var costText: String,
    var capacity: String,
    var attempts: String,
    var statusLabel: String,
)

fun toManualFreightInvoiceReviewView(item: ManualFreightInvoiceReview): ManualFreightInvoiceReviewView {
    return ManualFreightInvoiceReviewView(
        codeText = item.code,
        name = truncate(item.name, 80),
        costText = "%.2f".format(item.cost),
        capacity = item.capacity.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualFreightInvoiceReviewViews(items: MutableList<ManualFreightInvoiceReview>): MutableList<ManualFreightInvoiceReviewView> {
    val views = mutableListOf<ManualFreightInvoiceReviewView>()
    for (item in items) {
        views.add(toManualFreightInvoiceReviewView(item))
    }
    return views
}

fun sumManualFreightInvoiceReviewView(view: ManualFreightInvoiceReviewView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedCost + parsedCapacity.toDouble() + parsedAttempts.toDouble()
}
