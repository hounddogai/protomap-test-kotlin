package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global capacity agreement. */
data class GlobalCapacityAgreementView(
    var codeText: String,
    var title: String,
    var depotText: String,
    var timezone: String,
    var weight: String,
    var trailerIdText: String,
    var region: String,
    var pinned: String,
    var statusLabel: String,
)

fun toGlobalCapacityAgreementView(item: GlobalCapacityAgreement): GlobalCapacityAgreementView {
    return GlobalCapacityAgreementView(
        codeText = item.code,
        title = truncate(item.title, 80),
        depotText = item.depot,
        timezone = item.timezone,
        weight = "%.2f".format(item.weight),
        trailerIdText = item.trailerId,
        region = item.region,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCapacityAgreementViews(items: MutableList<GlobalCapacityAgreement>): MutableList<GlobalCapacityAgreementView> {
    val views = mutableListOf<GlobalCapacityAgreementView>()
    for (item in items) {
        views.add(toGlobalCapacityAgreementView(item))
    }
    return views
}

fun sumGlobalCapacityAgreementView(view: GlobalCapacityAgreementView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
