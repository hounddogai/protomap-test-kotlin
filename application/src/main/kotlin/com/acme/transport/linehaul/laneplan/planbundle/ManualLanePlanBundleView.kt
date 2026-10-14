package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual lane plan bundle. */
data class ManualLanePlanBundleView(
    var code: String,
    var labelText: String,
    var depot: String,
    var summary: String,
    var ratioText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toManualLanePlanBundleView(item: ManualLanePlanBundle): ManualLanePlanBundleView {
    return ManualLanePlanBundleView(
        code = item.code,
        labelText = item.label,
        depot = item.depot,
        summary = item.summary,
        ratioText = "%.2f".format(item.ratio),
        ownerId = truncate(item.ownerId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualLanePlanBundleViews(items: MutableList<ManualLanePlanBundle>): MutableList<ManualLanePlanBundleView> {
    val views = mutableListOf<ManualLanePlanBundleView>()
    for (item in items) {
        views.add(toManualLanePlanBundleView(item))
    }
    return views
}

fun sumManualLanePlanBundleView(view: ManualLanePlanBundleView): Double {
    val parsedRatio = view.ratioText.toDouble()
    return parsedRatio
}
