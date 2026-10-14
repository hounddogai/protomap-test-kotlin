package com.acme.transport.emissions.emissionfactor.request

/** How screens and exports show a emission factor request. */
data class EmissionFactorRequestView(
    var code: String,
    var label: String,
    var source: String,
    var batchSizeText: String,
    var ratioText: String,
    var carrier: String,
    var etaMinutesText: String,
    var country: String,
    var version: String,
    var statusLabel: String,
)

fun toEmissionFactorRequestView(item: EmissionFactorRequest): EmissionFactorRequestView {
    return EmissionFactorRequestView(
        code = item.code,
        label = item.label,
        source = item.source,
        batchSizeText = item.batchSize.toString(),
        ratioText = "%.2f".format(item.ratio),
        carrier = item.carrier,
        etaMinutesText = item.etaMinutes.toString(),
        country = item.country,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorRequestViews(items: MutableList<EmissionFactorRequest>): MutableList<EmissionFactorRequestView> {
    val views = mutableListOf<EmissionFactorRequestView>()
    for (item in items) {
        views.add(toEmissionFactorRequestView(item))
    }
    return views
}

fun sumEmissionFactorRequestView(view: EmissionFactorRequestView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedBatchSize.toDouble() + parsedRatio + parsedEtaMinutes.toDouble()
}
