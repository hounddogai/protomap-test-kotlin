package com.acme.transport.freightpay.freightinvoice.window

/** How screens and exports show a freight invoice threshold. */
data class FreightInvoiceThresholdView(
    var codeText: String,
    var label: String,
    var scoreText: String,
    var description: String,
    var palletsText: String,
    var carrier: String,
    var expiresAt: String,
    var stopCountText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toFreightInvoiceThresholdView(item: FreightInvoiceThreshold): FreightInvoiceThresholdView {
    return FreightInvoiceThresholdView(
        codeText = item.code,
        label = item.label,
        scoreText = "%.2f".format(item.score),
        description = item.description,
        palletsText = item.pallets.toString(),
        carrier = item.carrier,
        expiresAt = item.expiresAt.toString(),
        stopCountText = item.stopCount.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceThresholdViews(items: MutableList<FreightInvoiceThreshold>): MutableList<FreightInvoiceThresholdView> {
    val views = mutableListOf<FreightInvoiceThresholdView>()
    for (item in items) {
        views.add(toFreightInvoiceThresholdView(item))
    }
    return views
}

fun sumFreightInvoiceThresholdView(view: FreightInvoiceThresholdView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedScore + parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
