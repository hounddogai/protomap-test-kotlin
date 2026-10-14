package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy lane plan quota. */
data class LegacyLanePlanQuotaView(
    var code: String,
    var titleText: String,
    var phoneText: String,
    var odometer: String,
    var segment: String,
    var depotText: String,
    var sequence: String,
    var updatedAt: String,
    var countryText: String,
    var driverIdText: String,
    var discount: String,
    var statusLabel: String,
)

fun toLegacyLanePlanQuotaView(item: LegacyLanePlanQuota): LegacyLanePlanQuotaView {
    return LegacyLanePlanQuotaView(
        code = item.code,
        titleText = item.title,
        phoneText = truncate(item.phone, 64),
        odometer = item.odometer.toString(),
        segment = truncate(item.segment, 64),
        depotText = truncate(item.depot, 64),
        sequence = item.sequence.toString(),
        updatedAt = item.updatedAt.toString(),
        countryText = item.country,
        driverIdText = item.driverId,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLanePlanQuotaViews(items: MutableList<LegacyLanePlanQuota>): MutableList<LegacyLanePlanQuotaView> {
    val views = mutableListOf<LegacyLanePlanQuotaView>()
    for (item in items) {
        views.add(toLegacyLanePlanQuotaView(item))
    }
    return views
}

fun sumLegacyLanePlanQuotaView(view: LegacyLanePlanQuotaView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedOdometer.toDouble() + parsedSequence.toDouble() + parsedUpdatedAt.toDouble()
}
