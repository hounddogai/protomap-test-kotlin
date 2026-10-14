package com.acme.transport.emissions.emissionfactor.factorpreference

/** How screens and exports show a rolling emission factor agreement. */
data class RollingEmissionFactorAgreementView(
    var code: String,
    var label: String,
    var channelText: String,
    var createdAtText: String,
    var lane: String,
    var retriesText: String,
    var source: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toRollingEmissionFactorAgreementView(item: RollingEmissionFactorAgreement): RollingEmissionFactorAgreementView {
    return RollingEmissionFactorAgreementView(
        code = item.code,
        label = item.label,
        channelText = item.channel,
        createdAtText = item.createdAt.toString(),
        lane = item.lane,
        retriesText = item.retries.toString(),
        source = item.source,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingEmissionFactorAgreementViews(items: MutableList<RollingEmissionFactorAgreement>): MutableList<RollingEmissionFactorAgreementView> {
    val views = mutableListOf<RollingEmissionFactorAgreementView>()
    for (item in items) {
        views.add(toRollingEmissionFactorAgreementView(item))
    }
    return views
}

fun sumRollingEmissionFactorAgreementView(view: RollingEmissionFactorAgreementView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedCreatedAt.toDouble() + parsedRetries.toDouble() + parsedMinimum
}
