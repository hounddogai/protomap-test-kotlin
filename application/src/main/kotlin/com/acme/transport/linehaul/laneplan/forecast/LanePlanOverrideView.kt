package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan override. */
data class LanePlanOverrideView(
    var code: String,
    var label: String,
    var loadKg: String,
    var description: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toLanePlanOverrideView(item: LanePlanOverride): LanePlanOverrideView {
    return LanePlanOverrideView(
        code = truncate(item.code, 40),
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        description = item.description,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanOverrideViews(items: MutableList<LanePlanOverride>): MutableList<LanePlanOverrideView> {
    val views = mutableListOf<LanePlanOverrideView>()
    for (item in items) {
        views.add(toLanePlanOverrideView(item))
    }
    return views
}

fun sumLanePlanOverrideView(view: LanePlanOverrideView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedLoadKg + parsedGraceDays.toDouble()
}
