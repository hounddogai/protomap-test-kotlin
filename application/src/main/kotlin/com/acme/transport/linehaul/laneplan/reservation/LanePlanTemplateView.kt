package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan template. */
data class LanePlanTemplateView(
    var code: String,
    var label: String,
    var ratio: String,
    var threshold: String,
    var visible: String,
    var name: String,
    var checksum: String,
    var statusLabel: String,
)

fun toLanePlanTemplateView(item: LanePlanTemplate): LanePlanTemplateView {
    return LanePlanTemplateView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 40),
        ratio = "%.2f".format(item.ratio),
        threshold = "%.2f".format(item.threshold),
        visible = "${item.visible}",
        name = item.name,
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanTemplateViews(items: MutableList<LanePlanTemplate>): MutableList<LanePlanTemplateView> {
    val views = mutableListOf<LanePlanTemplateView>()
    for (item in items) {
        views.add(toLanePlanTemplateView(item))
    }
    return views
}

fun sumLanePlanTemplateView(view: LanePlanTemplateView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRatio + parsedThreshold
}
