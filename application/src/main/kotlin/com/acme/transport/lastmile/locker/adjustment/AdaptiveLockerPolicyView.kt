package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive locker policy. */
data class AdaptiveLockerPolicyView(
    var code: String,
    var name: String,
    var revision: String,
    var priorityText: String,
    var stopCount: String,
    var vehicleId: String,
    var batchSize: String,
    var driverIdText: String,
    var carrierText: String,
    var summaryText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toAdaptiveLockerPolicyView(item: AdaptiveLockerPolicy): AdaptiveLockerPolicyView {
    return AdaptiveLockerPolicyView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        revision = item.revision.toString(),
        priorityText = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        vehicleId = truncate(item.vehicleId, 40),
        batchSize = item.batchSize.toString(),
        driverIdText = item.driverId,
        carrierText = truncate(item.carrier, 80),
        summaryText = truncate(item.summary, 64),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLockerPolicyViews(items: MutableList<AdaptiveLockerPolicy>): MutableList<AdaptiveLockerPolicyView> {
    val views = mutableListOf<AdaptiveLockerPolicyView>()
    for (item in items) {
        views.add(toAdaptiveLockerPolicyView(item))
    }
    return views
}

fun sumAdaptiveLockerPolicyView(view: AdaptiveLockerPolicyView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRevision.toDouble() + parsedPriority.toDouble() + parsedStopCount.toDouble()
}
