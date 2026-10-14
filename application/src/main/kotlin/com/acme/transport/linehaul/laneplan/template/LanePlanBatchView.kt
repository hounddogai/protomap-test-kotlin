package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan batch. */
data class LanePlanBatchView(
    var code: String,
    var labelText: String,
    var stopCountText: String,
    var checksum: String,
    var revision: String,
    var statusLabel: String,
)

fun toLanePlanBatchView(item: LanePlanBatch): LanePlanBatchView {
    return LanePlanBatchView(
        code = item.code,
        labelText = truncate(item.label, 80),
        stopCountText = item.stopCount.toString(),
        checksum = item.checksum,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanBatchViews(items: MutableList<LanePlanBatch>): MutableList<LanePlanBatchView> {
    val views = mutableListOf<LanePlanBatchView>()
    for (item in items) {
        views.add(toLanePlanBatchView(item))
    }
    return views
}

fun sumLanePlanBatchView(view: LanePlanBatchView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedStopCount.toDouble() + parsedRevision.toDouble()
}
