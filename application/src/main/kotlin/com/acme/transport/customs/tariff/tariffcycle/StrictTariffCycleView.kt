package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tariff cycle. */
data class StrictTariffCycleView(
    var code: String,
    var title: String,
    var startsAtText: String,
    var notes: String,
    var visible: String,
    var statusLabel: String,
)

fun toStrictTariffCycleView(item: StrictTariffCycle): StrictTariffCycleView {
    return StrictTariffCycleView(
        code = item.code,
        title = truncate(item.title, 120),
        startsAtText = item.startsAt.toString(),
        notes = item.notes,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTariffCycleViews(items: MutableList<StrictTariffCycle>): MutableList<StrictTariffCycleView> {
    val views = mutableListOf<StrictTariffCycleView>()
    for (item in items) {
        views.add(toStrictTariffCycleView(item))
    }
    return views
}

fun sumStrictTariffCycleView(view: StrictTariffCycleView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStartsAt.toDouble()
}
