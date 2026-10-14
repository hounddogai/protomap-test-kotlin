package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss proposal. */
data class NearMissProposalView(
    var code: String,
    var label: String,
    var loadKg: String,
    var channelText: String,
    var startsAt: String,
    var durationMinutes: String,
    var endsAt: String,
    var routeCode: String,
    var region: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toNearMissProposalView(item: NearMissProposal): NearMissProposalView {
    return NearMissProposalView(
        code = item.code,
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        channelText = item.channel,
        startsAt = item.startsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        endsAt = item.endsAt.toString(),
        routeCode = item.routeCode,
        region = truncate(item.region, 80),
        phoneText = truncate(item.phone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissProposalViews(items: MutableList<NearMissProposal>): MutableList<NearMissProposalView> {
    val views = mutableListOf<NearMissProposalView>()
    for (item in items) {
        views.add(toNearMissProposalView(item))
    }
    return views
}

fun sumNearMissProposalView(view: NearMissProposalView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedLoadKg + parsedStartsAt.toDouble() + parsedDurationMinutes.toDouble()
}
