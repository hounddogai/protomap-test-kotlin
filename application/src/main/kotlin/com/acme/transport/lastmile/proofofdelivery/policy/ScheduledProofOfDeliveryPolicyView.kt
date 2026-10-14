package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled proof of delivery policy. */
data class ScheduledProofOfDeliveryPolicyView(
    var code: String,
    var name: String,
    var vehicleIdText: String,
    var externalRef: String,
    var activeText: String,
    var approved: String,
    var attempts: String,
    var categoryText: String,
    var rate: String,
    var statusLabel: String,
)

fun toScheduledProofOfDeliveryPolicyView(item: ScheduledProofOfDeliveryPolicy): ScheduledProofOfDeliveryPolicyView {
    return ScheduledProofOfDeliveryPolicyView(
        code = item.code,
        name = truncate(item.name, 80),
        vehicleIdText = item.vehicleId,
        externalRef = truncate(item.externalRef, 40),
        activeText = "${item.active}",
        approved = "${item.approved}",
        attempts = item.attempts.toString(),
        categoryText = item.category,
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledProofOfDeliveryPolicyViews(items: MutableList<ScheduledProofOfDeliveryPolicy>): MutableList<ScheduledProofOfDeliveryPolicyView> {
    val views = mutableListOf<ScheduledProofOfDeliveryPolicyView>()
    for (item in items) {
        views.add(toScheduledProofOfDeliveryPolicyView(item))
    }
    return views
}

fun sumScheduledProofOfDeliveryPolicyView(view: ScheduledProofOfDeliveryPolicyView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedAttempts.toDouble() + parsedRate
}
