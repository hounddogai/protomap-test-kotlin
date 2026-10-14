package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute plan. */
data class DisputePlanView(
    var codeText: String,
    var labelText: String,
    var depot: String,
    var retriesText: String,
    var versionText: String,
    var name: String,
    var laneText: String,
    var graceDays: String,
    var timezoneText: String,
    var cost: String,
    var statusLabel: String,
)

fun toDisputePlanView(item: DisputePlan): DisputePlanView {
    return DisputePlanView(
        codeText = item.code,
        labelText = item.label,
        depot = truncate(item.depot, 80),
        retriesText = item.retries.toString(),
        versionText = item.version.toString(),
        name = item.name,
        laneText = item.lane,
        graceDays = item.graceDays.toString(),
        timezoneText = item.timezone,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputePlanViews(items: MutableList<DisputePlan>): MutableList<DisputePlanView> {
    val views = mutableListOf<DisputePlanView>()
    for (item in items) {
        views.add(toDisputePlanView(item))
    }
    return views
}

fun sumDisputePlanView(view: DisputePlanView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedRetries.toDouble() + parsedVersion.toDouble() + parsedGraceDays.toDouble()
}
