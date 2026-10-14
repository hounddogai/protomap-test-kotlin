package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt batch. */
data class AttemptBatchView(
    var code: String,
    var nameText: String,
    var country: String,
    var rate: String,
    var driverIdText: String,
    var pinned: String,
    var dockText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toAttemptBatchView(item: AttemptBatch): AttemptBatchView {
    return AttemptBatchView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 64),
        country = item.country,
        rate = "%.2f".format(item.rate),
        driverIdText = item.driverId,
        pinned = "${item.pinned}",
        dockText = truncate(item.dock, 80),
        externalRef = truncate(item.externalRef, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptBatchViews(items: MutableList<AttemptBatch>): MutableList<AttemptBatchView> {
    val views = mutableListOf<AttemptBatchView>()
    for (item in items) {
        views.add(toAttemptBatchView(item))
    }
    return views
}

fun sumAttemptBatchView(view: AttemptBatchView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
