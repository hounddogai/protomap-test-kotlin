package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tire agreement. */
data class AdaptiveTireAgreementView(
    var codeText: String,
    var title: String,
    var budgetText: String,
    var dockText: String,
    var lane: String,
    var distanceKm: String,
    var country: String,
    var endsAtText: String,
    var marginText: String,
    var startsAtText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toAdaptiveTireAgreementView(item: AdaptiveTireAgreement): AdaptiveTireAgreementView {
    return AdaptiveTireAgreementView(
        codeText = item.code,
        title = item.title,
        budgetText = "%.2f".format(item.budget),
        dockText = truncate(item.dock, 40),
        lane = item.lane,
        distanceKm = "%.2f".format(item.distanceKm),
        country = item.country,
        endsAtText = item.endsAt.toString(),
        marginText = "%.2f".format(item.margin),
        startsAtText = item.startsAt.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTireAgreementViews(items: MutableList<AdaptiveTireAgreement>): MutableList<AdaptiveTireAgreementView> {
    val views = mutableListOf<AdaptiveTireAgreementView>()
    for (item in items) {
        views.add(toAdaptiveTireAgreementView(item))
    }
    return views
}

fun sumAdaptiveTireAgreementView(view: AdaptiveTireAgreementView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedBudget + parsedDistanceKm + parsedEndsAt.toDouble()
}
