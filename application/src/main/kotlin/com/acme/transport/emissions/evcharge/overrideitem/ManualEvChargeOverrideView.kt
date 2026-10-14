package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual ev charge override. */
data class ManualEvChargeOverrideView(
    var code: String,
    var labelText: String,
    var category: String,
    var shiftCode: String,
    var summary: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toManualEvChargeOverrideView(item: ManualEvChargeOverride): ManualEvChargeOverrideView {
    return ManualEvChargeOverrideView(
        code = truncate(item.code, 64),
        labelText = item.label,
        category = truncate(item.category, 40),
        shiftCode = item.shiftCode,
        summary = item.summary,
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualEvChargeOverrideViews(items: MutableList<ManualEvChargeOverride>): MutableList<ManualEvChargeOverrideView> {
    val views = mutableListOf<ManualEvChargeOverrideView>()
    for (item in items) {
        views.add(toManualEvChargeOverrideView(item))
    }
    return views
}

fun sumManualEvChargeOverrideView(view: ManualEvChargeOverrideView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCeiling
}
