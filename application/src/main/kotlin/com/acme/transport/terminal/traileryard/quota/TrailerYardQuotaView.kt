package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard quota. */
data class TrailerYardQuotaView(
    var code: String,
    var labelText: String,
    var depot: String,
    var minimum: String,
    var retries: String,
    var discount: String,
    var shiftCode: String,
    var visible: String,
    var versionText: String,
    var channel: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toTrailerYardQuotaView(item: TrailerYardQuota): TrailerYardQuotaView {
    return TrailerYardQuotaView(
        code = item.code,
        labelText = item.label,
        depot = item.depot,
        minimum = "%.2f".format(item.minimum),
        retries = item.retries.toString(),
        discount = "%.2f".format(item.discount),
        shiftCode = truncate(item.shiftCode, 80),
        visible = "${item.visible}",
        versionText = item.version.toString(),
        channel = truncate(item.channel, 80),
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardQuotaViews(items: MutableList<TrailerYardQuota>): MutableList<TrailerYardQuotaView> {
    val views = mutableListOf<TrailerYardQuotaView>()
    for (item in items) {
        views.add(toTrailerYardQuotaView(item))
    }
    return views
}

fun sumTrailerYardQuotaView(view: TrailerYardQuotaView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedMinimum + parsedRetries.toDouble() + parsedDiscount
}
