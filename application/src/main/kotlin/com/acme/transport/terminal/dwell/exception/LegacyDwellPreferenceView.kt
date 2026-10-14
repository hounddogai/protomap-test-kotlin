package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy dwell preference. */
data class LegacyDwellPreferenceView(
    var codeText: String,
    var nameText: String,
    var batchSize: String,
    var unit: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toLegacyDwellPreferenceView(item: LegacyDwellPreference): LegacyDwellPreferenceView {
    return LegacyDwellPreferenceView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        batchSize = item.batchSize.toString(),
        unit = item.unit,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDwellPreferenceViews(items: MutableList<LegacyDwellPreference>): MutableList<LegacyDwellPreferenceView> {
    val views = mutableListOf<LegacyDwellPreferenceView>()
    for (item in items) {
        views.add(toLegacyDwellPreferenceView(item))
    }
    return views
}

fun sumLegacyDwellPreferenceView(view: LegacyDwellPreferenceView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
