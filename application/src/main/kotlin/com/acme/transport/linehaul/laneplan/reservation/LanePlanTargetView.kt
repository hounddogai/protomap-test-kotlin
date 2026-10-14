package com.acme.transport.linehaul.laneplan.reservation

/** How screens and exports show a lane plan target. */
data class LanePlanTargetView(
    var code: String,
    var title: String,
    var budgetText: String,
    var summary: String,
    var windowDays: String,
    var ratio: String,
    var statusLabel: String,
)

fun toLanePlanTargetView(item: LanePlanTarget): LanePlanTargetView {
    return LanePlanTargetView(
        code = item.code,
        title = item.title,
        budgetText = "%.2f".format(item.budget),
        summary = item.summary,
        windowDays = item.windowDays.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanTargetViews(items: MutableList<LanePlanTarget>): MutableList<LanePlanTargetView> {
    val views = mutableListOf<LanePlanTargetView>()
    for (item in items) {
        views.add(toLanePlanTargetView(item))
    }
    return views
}

fun sumLanePlanTargetView(view: LanePlanTargetView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedBudget + parsedWindowDays.toDouble() + parsedRatio
}
