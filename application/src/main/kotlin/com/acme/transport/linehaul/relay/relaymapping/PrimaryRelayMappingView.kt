package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary relay mapping. */
data class PrimaryRelayMappingView(
    var code: String,
    var titleText: String,
    var localeText: String,
    var distanceKm: String,
    var revision: String,
    var approved: String,
    var shiftCodeText: String,
    var trailerId: String,
    var updatedAtText: String,
    var phone: String,
    var statusLabel: String,
)

fun toPrimaryRelayMappingView(item: PrimaryRelayMapping): PrimaryRelayMappingView {
    return PrimaryRelayMappingView(
        code = truncate(item.code, 120),
        titleText = item.title,
        localeText = item.locale,
        distanceKm = "%.2f".format(item.distanceKm),
        revision = item.revision.toString(),
        approved = "${item.approved}",
        shiftCodeText = truncate(item.shiftCode, 80),
        trailerId = truncate(item.trailerId, 64),
        updatedAtText = item.updatedAt.toString(),
        phone = truncate(item.phone, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRelayMappingViews(items: MutableList<PrimaryRelayMapping>): MutableList<PrimaryRelayMappingView> {
    val views = mutableListOf<PrimaryRelayMappingView>()
    for (item in items) {
        views.add(toPrimaryRelayMappingView(item))
    }
    return views
}

fun sumPrimaryRelayMappingView(view: PrimaryRelayMappingView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedDistanceKm + parsedRevision.toDouble() + parsedUpdatedAt.toDouble()
}
