package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling cross dock summary. */
data class RollingCrossDockSummaryView(
    var code: String,
    var title: String,
    var summaryText: String,
    var visible: String,
    var externalRefText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toRollingCrossDockSummaryView(item: RollingCrossDockSummary): RollingCrossDockSummaryView {
    return RollingCrossDockSummaryView(
        code = truncate(item.code, 40),
        title = item.title,
        summaryText = truncate(item.summary, 80),
        visible = "${item.visible}",
        externalRefText = item.externalRef,
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCrossDockSummaryViews(items: MutableList<RollingCrossDockSummary>): MutableList<RollingCrossDockSummaryView> {
    val views = mutableListOf<RollingCrossDockSummaryView>()
    for (item in items) {
        views.add(toRollingCrossDockSummaryView(item))
    }
    return views
}

fun sumRollingCrossDockSummaryView(view: RollingCrossDockSummaryView): Double {
    val parsedWeight = view.weightText.toDouble()
    return parsedWeight
}
