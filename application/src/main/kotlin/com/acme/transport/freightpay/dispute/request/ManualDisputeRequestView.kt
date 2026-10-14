package com.acme.transport.freightpay.dispute.request

/** How screens and exports show a manual dispute request. */
data class ManualDisputeRequestView(
    var codeText: String,
    var labelText: String,
    var contactEmailText: String,
    var graceDays: String,
    var approved: String,
    var discount: String,
    var batchSize: String,
    var weight: String,
    var active: String,
    var driverId: String,
    var statusLabel: String,
)

fun toManualDisputeRequestView(item: ManualDisputeRequest): ManualDisputeRequestView {
    return ManualDisputeRequestView(
        codeText = item.code,
        labelText = item.label,
        contactEmailText = item.contactEmail,
        graceDays = item.graceDays.toString(),
        approved = "${item.approved}",
        discount = "%.2f".format(item.discount),
        batchSize = item.batchSize.toString(),
        weight = "%.2f".format(item.weight),
        active = "${item.active}",
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDisputeRequestViews(items: MutableList<ManualDisputeRequest>): MutableList<ManualDisputeRequestView> {
    val views = mutableListOf<ManualDisputeRequestView>()
    for (item in items) {
        views.add(toManualDisputeRequestView(item))
    }
    return views
}

fun sumManualDisputeRequestView(view: ManualDisputeRequestView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedGraceDays.toDouble() + parsedDiscount + parsedBatchSize.toDouble()
}
