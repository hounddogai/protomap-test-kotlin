package com.acme.transport.freightpay.freightinvoice.entry

/** How screens and exports show a tiered freight invoice checkpoint. */
data class TieredFreightInvoiceCheckpointView(
    var code: String,
    var nameText: String,
    var depot: String,
    var budget: String,
    var label: String,
    var createdAt: String,
    var visible: String,
    var active: String,
    var contactEmail: String,
    var stopCount: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toTieredFreightInvoiceCheckpointView(item: TieredFreightInvoiceCheckpoint): TieredFreightInvoiceCheckpointView {
    return TieredFreightInvoiceCheckpointView(
        code = item.code,
        nameText = item.name,
        depot = item.depot,
        budget = "%.2f".format(item.budget),
        label = item.label,
        createdAt = item.createdAt.toString(),
        visible = "${item.visible}",
        active = "${item.active}",
        contactEmail = item.contactEmail,
        stopCount = item.stopCount.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFreightInvoiceCheckpointViews(items: MutableList<TieredFreightInvoiceCheckpoint>): MutableList<TieredFreightInvoiceCheckpointView> {
    val views = mutableListOf<TieredFreightInvoiceCheckpointView>()
    for (item in items) {
        views.add(toTieredFreightInvoiceCheckpointView(item))
    }
    return views
}

fun sumTieredFreightInvoiceCheckpointView(view: TieredFreightInvoiceCheckpointView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedBudget + parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}
