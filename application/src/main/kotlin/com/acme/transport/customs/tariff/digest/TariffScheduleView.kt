package com.acme.transport.customs.tariff.digest

/** How screens and exports show a tariff schedule. */
data class TariffScheduleView(
    var code: String,
    var title: String,
    var etaMinutesText: String,
    var country: String,
    var endsAt: String,
    var discount: String,
    var sequence: String,
    var startsAt: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toTariffScheduleView(item: TariffSchedule): TariffScheduleView {
    return TariffScheduleView(
        code = item.code,
        title = item.title,
        etaMinutesText = item.etaMinutes.toString(),
        country = item.country,
        endsAt = item.endsAt.toString(),
        discount = "%.2f".format(item.discount),
        sequence = item.sequence.toString(),
        startsAt = item.startsAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffScheduleViews(items: MutableList<TariffSchedule>): MutableList<TariffScheduleView> {
    val views = mutableListOf<TariffScheduleView>()
    for (item in items) {
        views.add(toTariffScheduleView(item))
    }
    return views
}

fun sumTariffScheduleView(view: TariffScheduleView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedEtaMinutes.toDouble() + parsedEndsAt.toDouble() + parsedDiscount
}
