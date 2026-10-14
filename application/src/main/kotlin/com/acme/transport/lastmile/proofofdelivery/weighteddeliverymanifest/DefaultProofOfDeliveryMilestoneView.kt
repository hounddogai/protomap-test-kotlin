package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default proof of delivery milestone. */
data class DefaultProofOfDeliveryMilestoneView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var driverId: String,
    var segment: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toDefaultProofOfDeliveryMilestoneView(item: DefaultProofOfDeliveryMilestone): DefaultProofOfDeliveryMilestoneView {
    return DefaultProofOfDeliveryMilestoneView(
        codeText = truncate(item.code, 40),
        name = item.name,
        attempts = item.attempts.toString(),
        driverId = item.driverId,
        segment = item.segment,
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultProofOfDeliveryMilestoneViews(items: MutableList<DefaultProofOfDeliveryMilestone>): MutableList<DefaultProofOfDeliveryMilestoneView> {
    val views = mutableListOf<DefaultProofOfDeliveryMilestoneView>()
    for (item in items) {
        views.add(toDefaultProofOfDeliveryMilestoneView(item))
    }
    return views
}

fun sumDefaultProofOfDeliveryMilestoneView(view: DefaultProofOfDeliveryMilestoneView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
