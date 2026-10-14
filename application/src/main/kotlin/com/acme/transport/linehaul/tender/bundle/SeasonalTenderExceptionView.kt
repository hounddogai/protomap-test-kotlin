package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal tender exception. */
data class SeasonalTenderExceptionView(
    var code: String,
    var title: String,
    var endsAtText: String,
    var ratioText: String,
    var notes: String,
    var channel: String,
    var vehicleId: String,
    var dock: String,
    var etaMinutes: String,
    var shiftCode: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toSeasonalTenderExceptionView(item: SeasonalTenderException): SeasonalTenderExceptionView {
    return SeasonalTenderExceptionView(
        code = truncate(item.code, 80),
        title = item.title,
        endsAtText = item.endsAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        notes = item.notes,
        channel = item.channel,
        vehicleId = item.vehicleId,
        dock = truncate(item.dock, 120),
        etaMinutes = item.etaMinutes.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTenderExceptionViews(items: MutableList<SeasonalTenderException>): MutableList<SeasonalTenderExceptionView> {
    val views = mutableListOf<SeasonalTenderExceptionView>()
    for (item in items) {
        views.add(toSeasonalTenderExceptionView(item))
    }
    return views
}

fun sumSeasonalTenderExceptionView(view: SeasonalTenderExceptionView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEndsAt.toDouble() + parsedRatio + parsedEtaMinutes.toDouble()
}
