package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cutoff schedule. */
data class DefaultCutoffScheduleView(
    var codeText: String,
    var label: String,
    var externalRefText: String,
    var pallets: String,
    var ceiling: String,
    var shiftCode: String,
    var ratio: String,
    var statusLabel: String,
)

fun toDefaultCutoffScheduleView(item: DefaultCutoffSchedule): DefaultCutoffScheduleView {
    return DefaultCutoffScheduleView(
        codeText = item.code,
        label = item.label,
        externalRefText = truncate(item.externalRef, 64),
        pallets = item.pallets.toString(),
        ceiling = "%.2f".format(item.ceiling),
        shiftCode = truncate(item.shiftCode, 40),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCutoffScheduleViews(items: MutableList<DefaultCutoffSchedule>): MutableList<DefaultCutoffScheduleView> {
    val views = mutableListOf<DefaultCutoffScheduleView>()
    for (item in items) {
        views.add(toDefaultCutoffScheduleView(item))
    }
    return views
}

fun sumDefaultCutoffScheduleView(view: DefaultCutoffScheduleView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedPallets.toDouble() + parsedCeiling + parsedRatio
}
