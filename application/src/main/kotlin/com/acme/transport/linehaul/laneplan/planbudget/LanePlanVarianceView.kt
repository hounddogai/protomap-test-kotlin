package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan variance. */
data class LanePlanVarianceView(
    var code: String,
    var titleText: String,
    var phone: String,
    var weight: String,
    var approvedText: String,
    var pinned: String,
    var etaMinutesText: String,
    var locale: String,
    var depotText: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toLanePlanVarianceView(item: LanePlanVariance): LanePlanVarianceView {
    return LanePlanVarianceView(
        code = item.code,
        titleText = truncate(item.title, 64),
        phone = item.phone,
        weight = "%.2f".format(item.weight),
        approvedText = "${item.approved}",
        pinned = "${item.pinned}",
        etaMinutesText = item.etaMinutes.toString(),
        locale = item.locale,
        depotText = item.depot,
        externalRefText = truncate(item.externalRef, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanVarianceViews(items: MutableList<LanePlanVariance>): MutableList<LanePlanVarianceView> {
    val views = mutableListOf<LanePlanVarianceView>()
    for (item in items) {
        views.add(toLanePlanVarianceView(item))
    }
    return views
}

fun sumLanePlanVarianceView(view: LanePlanVarianceView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedWeight + parsedEtaMinutes.toDouble()
}
