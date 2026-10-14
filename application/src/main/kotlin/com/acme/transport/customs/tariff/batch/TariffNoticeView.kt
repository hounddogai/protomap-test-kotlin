package com.acme.transport.customs.tariff.batch

/** How screens and exports show a tariff notice. */
data class TariffNoticeView(
    var codeText: String,
    var titleText: String,
    var externalRef: String,
    var budget: String,
    var carrier: String,
    var etaMinutesText: String,
    var segment: String,
    var updatedAt: String,
    var active: String,
    var sequence: String,
    var statusLabel: String,
)

fun toTariffNoticeView(item: TariffNotice): TariffNoticeView {
    return TariffNoticeView(
        codeText = item.code,
        titleText = item.title,
        externalRef = item.externalRef,
        budget = "%.2f".format(item.budget),
        carrier = item.carrier,
        etaMinutesText = item.etaMinutes.toString(),
        segment = item.segment,
        updatedAt = item.updatedAt.toString(),
        active = "${item.active}",
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffNoticeViews(items: MutableList<TariffNotice>): MutableList<TariffNoticeView> {
    val views = mutableListOf<TariffNoticeView>()
    for (item in items) {
        views.add(toTariffNoticeView(item))
    }
    return views
}

fun sumTariffNoticeView(view: TariffNoticeView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedBudget + parsedEtaMinutes.toDouble() + parsedUpdatedAt.toDouble()
}
