package com.acme.transport.freightpay.freightinvoice.schedule

/** How screens and exports show a fallback freight invoice checkpoint. */
data class FallbackFreightInvoiceCheckpointView(
    var code: String,
    var nameText: String,
    var reviewerText: String,
    var contactEmail: String,
    var createdAt: String,
    var endsAt: String,
    var graceDays: String,
    var source: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toFallbackFreightInvoiceCheckpointView(item: FallbackFreightInvoiceCheckpoint): FallbackFreightInvoiceCheckpointView {
    return FallbackFreightInvoiceCheckpointView(
        code = item.code,
        nameText = item.name,
        reviewerText = item.reviewer,
        contactEmail = item.contactEmail,
        createdAt = item.createdAt.toString(),
        endsAt = item.endsAt.toString(),
        graceDays = item.graceDays.toString(),
        source = item.source,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackFreightInvoiceCheckpointViews(items: MutableList<FallbackFreightInvoiceCheckpoint>): MutableList<FallbackFreightInvoiceCheckpointView> {
    val views = mutableListOf<FallbackFreightInvoiceCheckpointView>()
    for (item in items) {
        views.add(toFallbackFreightInvoiceCheckpointView(item))
    }
    return views
}

fun sumFallbackFreightInvoiceCheckpointView(view: FallbackFreightInvoiceCheckpointView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCreatedAt.toDouble() + parsedEndsAt.toDouble() + parsedGraceDays.toDouble()
}
