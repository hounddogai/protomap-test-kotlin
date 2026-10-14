package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dwell template. */
data class RollingDwellTemplateView(
    var code: String,
    var nameText: String,
    var fuelLiters: String,
    var score: String,
    var endsAt: String,
    var pinnedText: String,
    var depot: String,
    var shiftCode: String,
    var category: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toRollingDwellTemplateView(item: RollingDwellTemplate): RollingDwellTemplateView {
    return RollingDwellTemplateView(
        code = item.code,
        nameText = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        score = "%.2f".format(item.score),
        endsAt = item.endsAt.toString(),
        pinnedText = "${item.pinned}",
        depot = truncate(item.depot, 80),
        shiftCode = item.shiftCode,
        category = item.category,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDwellTemplateViews(items: MutableList<RollingDwellTemplate>): MutableList<RollingDwellTemplateView> {
    val views = mutableListOf<RollingDwellTemplateView>()
    for (item in items) {
        views.add(toRollingDwellTemplateView(item))
    }
    return views
}

fun sumRollingDwellTemplateView(view: RollingDwellTemplateView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedFuelLiters + parsedScore + parsedEndsAt.toDouble()
}
