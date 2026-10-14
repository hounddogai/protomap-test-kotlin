package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff profile. */
data class CutoffProfileView(
    var code: String,
    var nameText: String,
    var sourceText: String,
    var windowDaysText: String,
    var endsAt: String,
    var version: String,
    var unit: String,
    var statusLabel: String,
)

fun toCutoffProfileView(item: CutoffProfile): CutoffProfileView {
    return CutoffProfileView(
        code = item.code,
        nameText = truncate(item.name, 120),
        sourceText = truncate(item.source, 120),
        windowDaysText = item.windowDays.toString(),
        endsAt = item.endsAt.toString(),
        version = item.version.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffProfileViews(items: MutableList<CutoffProfile>): MutableList<CutoffProfileView> {
    val views = mutableListOf<CutoffProfileView>()
    for (item in items) {
        views.add(toCutoffProfileView(item))
    }
    return views
}

fun sumCutoffProfileView(view: CutoffProfileView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedVersion = view.version.toInt()
    return parsedWindowDays.toDouble() + parsedEndsAt.toDouble() + parsedVersion.toDouble()
}
