package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial policy. */
data class AccessorialPolicyView(
    var code: String,
    var label: String,
    var segment: String,
    var limit: String,
    var odometer: String,
    var visible: String,
    var fuelLitersText: String,
    var region: String,
    var threshold: String,
    var stopCount: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toAccessorialPolicyView(item: AccessorialPolicy): AccessorialPolicyView {
    return AccessorialPolicyView(
        code = item.code,
        label = truncate(item.label, 40),
        segment = item.segment,
        limit = item.limit.toString(),
        odometer = item.odometer.toString(),
        visible = "${item.visible}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        region = item.region,
        threshold = "%.2f".format(item.threshold),
        stopCount = item.stopCount.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialPolicyViews(items: MutableList<AccessorialPolicy>): MutableList<AccessorialPolicyView> {
    val views = mutableListOf<AccessorialPolicyView>()
    for (item in items) {
        views.add(toAccessorialPolicyView(item))
    }
    return views
}

fun sumAccessorialPolicyView(view: AccessorialPolicyView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedLimit.toDouble() + parsedOdometer.toDouble() + parsedFuelLiters
}
