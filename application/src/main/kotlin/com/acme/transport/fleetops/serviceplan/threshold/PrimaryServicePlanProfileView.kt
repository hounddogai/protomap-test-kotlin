package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary service plan profile. */
data class PrimaryServicePlanProfileView(
    var code: String,
    var label: String,
    var score: String,
    var shiftCode: String,
    var etaMinutes: String,
    var loadKg: String,
    var discount: String,
    var archived: String,
    var capacity: String,
    var ownerId: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toPrimaryServicePlanProfileView(item: PrimaryServicePlanProfile): PrimaryServicePlanProfileView {
    return PrimaryServicePlanProfileView(
        code = item.code,
        label = item.label,
        score = "%.2f".format(item.score),
        shiftCode = item.shiftCode,
        etaMinutes = item.etaMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        discount = "%.2f".format(item.discount),
        archived = "${item.archived}",
        capacity = item.capacity.toString(),
        ownerId = truncate(item.ownerId, 64),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServicePlanProfileViews(items: MutableList<PrimaryServicePlanProfile>): MutableList<PrimaryServicePlanProfileView> {
    val views = mutableListOf<PrimaryServicePlanProfileView>()
    for (item in items) {
        views.add(toPrimaryServicePlanProfileView(item))
    }
    return views
}

fun sumPrimaryServicePlanProfileView(view: PrimaryServicePlanProfileView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedScore + parsedEtaMinutes.toDouble() + parsedLoadKg
}
