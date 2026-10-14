package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched locker claim. */
data class BatchedLockerClaimView(
    var codeText: String,
    var name: String,
    var driverIdText: String,
    var region: String,
    var segment: String,
    var country: String,
    var dock: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toBatchedLockerClaimView(item: BatchedLockerClaim): BatchedLockerClaimView {
    return BatchedLockerClaimView(
        codeText = item.code,
        name = item.name,
        driverIdText = item.driverId,
        region = item.region,
        segment = truncate(item.segment, 120),
        country = item.country,
        dock = truncate(item.dock, 40),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLockerClaimViews(items: MutableList<BatchedLockerClaim>): MutableList<BatchedLockerClaimView> {
    val views = mutableListOf<BatchedLockerClaimView>()
    for (item in items) {
        views.add(toBatchedLockerClaimView(item))
    }
    return views
}

fun sumBatchedLockerClaimView(view: BatchedLockerClaimView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}
