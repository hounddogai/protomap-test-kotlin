package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive zone agreement. */
data class AdaptiveZoneAgreementView(
    var code: String,
    var labelText: String,
    var phone: String,
    var sourceText: String,
    var updatedAt: String,
    var archived: String,
    var statusLabel: String,
)

fun toAdaptiveZoneAgreementView(item: AdaptiveZoneAgreement): AdaptiveZoneAgreementView {
    return AdaptiveZoneAgreementView(
        code = item.code,
        labelText = truncate(item.label, 64),
        phone = truncate(item.phone, 120),
        sourceText = truncate(item.source, 40),
        updatedAt = item.updatedAt.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveZoneAgreementViews(items: MutableList<AdaptiveZoneAgreement>): MutableList<AdaptiveZoneAgreementView> {
    val views = mutableListOf<AdaptiveZoneAgreementView>()
    for (item in items) {
        views.add(toAdaptiveZoneAgreementView(item))
    }
    return views
}

fun sumAdaptiveZoneAgreementView(view: AdaptiveZoneAgreementView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
