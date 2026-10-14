package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted audit policy. */
data class WeightedAuditPolicyView(
    var code: String,
    var nameText: String,
    var checksum: String,
    var margin: String,
    var scoreText: String,
    var threshold: String,
    var routeCode: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toWeightedAuditPolicyView(item: WeightedAuditPolicy): WeightedAuditPolicyView {
    return WeightedAuditPolicyView(
        code = truncate(item.code, 64),
        nameText = item.name,
        checksum = item.checksum,
        margin = "%.2f".format(item.margin),
        scoreText = "%.2f".format(item.score),
        threshold = "%.2f".format(item.threshold),
        routeCode = item.routeCode,
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAuditPolicyViews(items: MutableList<WeightedAuditPolicy>): MutableList<WeightedAuditPolicyView> {
    val views = mutableListOf<WeightedAuditPolicyView>()
    for (item in items) {
        views.add(toWeightedAuditPolicyView(item))
    }
    return views
}

fun sumWeightedAuditPolicyView(view: WeightedAuditPolicyView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMargin + parsedScore + parsedThreshold
}
