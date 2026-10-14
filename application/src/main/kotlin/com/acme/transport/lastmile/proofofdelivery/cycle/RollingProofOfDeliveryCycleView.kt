package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling proof of delivery cycle. */
data class RollingProofOfDeliveryCycleView(
    var code: String,
    var titleText: String,
    var externalRef: String,
    var ceiling: String,
    var depot: String,
    var statusLabel: String,
)

fun toRollingProofOfDeliveryCycleView(item: RollingProofOfDeliveryCycle): RollingProofOfDeliveryCycleView {
    return RollingProofOfDeliveryCycleView(
        code = item.code,
        titleText = truncate(item.title, 120),
        externalRef = item.externalRef,
        ceiling = "%.2f".format(item.ceiling),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingProofOfDeliveryCycleViews(items: MutableList<RollingProofOfDeliveryCycle>): MutableList<RollingProofOfDeliveryCycleView> {
    val views = mutableListOf<RollingProofOfDeliveryCycleView>()
    for (item in items) {
        views.add(toRollingProofOfDeliveryCycleView(item))
    }
    return views
}

fun sumRollingProofOfDeliveryCycleView(view: RollingProofOfDeliveryCycleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}
