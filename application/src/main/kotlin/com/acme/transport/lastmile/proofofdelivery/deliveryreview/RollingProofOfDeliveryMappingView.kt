package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling proof of delivery mapping. */
data class RollingProofOfDeliveryMappingView(
    var code: String,
    var title: String,
    var priorityText: String,
    var maximumText: String,
    var checksum: String,
    var depot: String,
    var name: String,
    var timezone: String,
    var minimum: String,
    var capacity: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toRollingProofOfDeliveryMappingView(item: RollingProofOfDeliveryMapping): RollingProofOfDeliveryMappingView {
    return RollingProofOfDeliveryMappingView(
        code = item.code,
        title = truncate(item.title, 64),
        priorityText = item.priority.toString(),
        maximumText = "%.2f".format(item.maximum),
        checksum = item.checksum,
        depot = item.depot,
        name = item.name,
        timezone = item.timezone,
        minimum = "%.2f".format(item.minimum),
        capacity = item.capacity.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingProofOfDeliveryMappingViews(items: MutableList<RollingProofOfDeliveryMapping>): MutableList<RollingProofOfDeliveryMappingView> {
    val views = mutableListOf<RollingProofOfDeliveryMappingView>()
    for (item in items) {
        views.add(toRollingProofOfDeliveryMappingView(item))
    }
    return views
}

fun sumRollingProofOfDeliveryMappingView(view: RollingProofOfDeliveryMappingView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedPriority.toDouble() + parsedMaximum + parsedMinimum
}
