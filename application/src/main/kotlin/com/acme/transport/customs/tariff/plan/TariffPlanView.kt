package com.acme.transport.customs.tariff.plan

/** How screens and exports show a tariff plan. */
data class TariffPlanView(
    var code: String,
    var title: String,
    var capacity: String,
    var region: String,
    var shiftCode: String,
    var endsAtText: String,
    var source: String,
    var statusLabel: String,
)

fun toTariffPlanView(item: TariffPlan): TariffPlanView {
    return TariffPlanView(
        code = item.code,
        title = item.title,
        capacity = item.capacity.toString(),
        region = item.region,
        shiftCode = item.shiftCode,
        endsAtText = item.endsAt.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffPlanViews(items: MutableList<TariffPlan>): MutableList<TariffPlanView> {
    val views = mutableListOf<TariffPlanView>()
    for (item in items) {
        views.add(toTariffPlanView(item))
    }
    return views
}

fun sumTariffPlanView(view: TariffPlanView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedCapacity.toDouble() + parsedEndsAt.toDouble()
}
