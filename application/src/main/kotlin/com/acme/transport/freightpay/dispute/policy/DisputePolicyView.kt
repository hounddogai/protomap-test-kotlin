package com.acme.transport.freightpay.dispute.policy

/** How screens and exports show a dispute policy. */
data class DisputePolicyView(
    var code: String,
    var label: String,
    var driverId: String,
    var etaMinutesText: String,
    var country: String,
    var retriesText: String,
    var versionText: String,
    var locale: String,
    var vehicleIdText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toDisputePolicyView(item: DisputePolicy): DisputePolicyView {
    return DisputePolicyView(
        code = item.code,
        label = item.label,
        driverId = item.driverId,
        etaMinutesText = item.etaMinutes.toString(),
        country = item.country,
        retriesText = item.retries.toString(),
        versionText = item.version.toString(),
        locale = item.locale,
        vehicleIdText = item.vehicleId,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputePolicyViews(items: MutableList<DisputePolicy>): MutableList<DisputePolicyView> {
    val views = mutableListOf<DisputePolicyView>()
    for (item in items) {
        views.add(toDisputePolicyView(item))
    }
    return views
}

fun sumDisputePolicyView(view: DisputePolicyView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedEtaMinutes.toDouble() + parsedRetries.toDouble() + parsedVersion.toDouble()
}
