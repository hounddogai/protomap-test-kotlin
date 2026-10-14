package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop variance. */
data class DropVarianceView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var lane: String,
    var depot: String,
    var contactEmailText: String,
    var label: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toDropVarianceView(item: DropVariance): DropVarianceView {
    return DropVarianceView(
        codeText = item.code,
        name = truncate(item.name, 64),
        batchSize = item.batchSize.toString(),
        lane = item.lane,
        depot = item.depot,
        contactEmailText = item.contactEmail,
        label = item.label,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropVarianceViews(items: MutableList<DropVariance>): MutableList<DropVarianceView> {
    val views = mutableListOf<DropVarianceView>()
    for (item in items) {
        views.add(toDropVarianceView(item))
    }
    return views
}

fun sumDropVarianceView(view: DropVarianceView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
