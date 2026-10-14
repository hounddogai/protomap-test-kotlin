package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker draft. */
data class BrokerDraftView(
    var code: String,
    var label: String,
    var countryText: String,
    var versionText: String,
    var carrier: String,
    var revision: String,
    var weight: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toBrokerDraftView(item: BrokerDraft): BrokerDraftView {
    return BrokerDraftView(
        code = truncate(item.code, 120),
        label = item.label,
        countryText = item.country,
        versionText = item.version.toString(),
        carrier = truncate(item.carrier, 40),
        revision = item.revision.toString(),
        weight = "%.2f".format(item.weight),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerDraftViews(items: MutableList<BrokerDraft>): MutableList<BrokerDraftView> {
    val views = mutableListOf<BrokerDraftView>()
    for (item in items) {
        views.add(toBrokerDraftView(item))
    }
    return views
}

fun sumBrokerDraftView(view: BrokerDraftView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedVersion.toDouble() + parsedRevision.toDouble() + parsedWeight
}
