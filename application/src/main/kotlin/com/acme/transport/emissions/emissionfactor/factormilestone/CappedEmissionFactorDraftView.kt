package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped emission factor draft. */
data class CappedEmissionFactorDraftView(
    var code: String,
    var label: String,
    var maximumText: String,
    var versionText: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toCappedEmissionFactorDraftView(item: CappedEmissionFactorDraft): CappedEmissionFactorDraftView {
    return CappedEmissionFactorDraftView(
        code = item.code,
        label = truncate(item.label, 80),
        maximumText = "%.2f".format(item.maximum),
        versionText = item.version.toString(),
        driverIdText = truncate(item.driverId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedEmissionFactorDraftViews(items: MutableList<CappedEmissionFactorDraft>): MutableList<CappedEmissionFactorDraftView> {
    val views = mutableListOf<CappedEmissionFactorDraftView>()
    for (item in items) {
        views.add(toCappedEmissionFactorDraftView(item))
    }
    return views
}

fun sumCappedEmissionFactorDraftView(view: CappedEmissionFactorDraftView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedMaximum + parsedVersion.toDouble()
}
