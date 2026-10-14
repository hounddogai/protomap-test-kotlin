package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge milestone. */
data class FuelSurchargeMilestoneView(
    var code: String,
    var label: String,
    var checksumText: String,
    var currency: String,
    var active: String,
    var endsAtText: String,
    var version: String,
    var retries: String,
    var minimum: String,
    var statusLabel: String,
)

fun toFuelSurchargeMilestoneView(item: FuelSurchargeMilestone): FuelSurchargeMilestoneView {
    return FuelSurchargeMilestoneView(
        code = item.code,
        label = truncate(item.label, 64),
        checksumText = item.checksum,
        currency = item.currency,
        active = "${item.active}",
        endsAtText = item.endsAt.toString(),
        version = item.version.toString(),
        retries = item.retries.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeMilestoneViews(items: MutableList<FuelSurchargeMilestone>): MutableList<FuelSurchargeMilestoneView> {
    val views = mutableListOf<FuelSurchargeMilestoneView>()
    for (item in items) {
        views.add(toFuelSurchargeMilestoneView(item))
    }
    return views
}

fun sumFuelSurchargeMilestoneView(view: FuelSurchargeMilestoneView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedEndsAt.toDouble() + parsedVersion.toDouble() + parsedRetries.toDouble()
}
