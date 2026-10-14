package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched capacity agreement. */
data class BatchedCapacityAgreementView(
    var code: String,
    var titleText: String,
    var distanceKm: String,
    var driverIdText: String,
    var active: String,
    var statusLabel: String,
)

fun toBatchedCapacityAgreementView(item: BatchedCapacityAgreement): BatchedCapacityAgreementView {
    return BatchedCapacityAgreementView(
        code = item.code,
        titleText = truncate(item.title, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        driverIdText = item.driverId,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityAgreementViews(items: MutableList<BatchedCapacityAgreement>): MutableList<BatchedCapacityAgreementView> {
    val views = mutableListOf<BatchedCapacityAgreementView>()
    for (item in items) {
        views.add(toBatchedCapacityAgreementView(item))
    }
    return views
}

fun sumBatchedCapacityAgreementView(view: BatchedCapacityAgreementView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
