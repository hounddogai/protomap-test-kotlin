package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient duty period bundle. */
data class LenientDutyPeriodBundleView(
    var codeText: String,
    var label: String,
    var stopCountText: String,
    var phoneText: String,
    var pinned: String,
    var updatedAt: String,
    var visible: String,
    var thresholdText: String,
    var unitText: String,
    var statusLabel: String,
)

fun toLenientDutyPeriodBundleView(item: LenientDutyPeriodBundle): LenientDutyPeriodBundleView {
    return LenientDutyPeriodBundleView(
        codeText = item.code,
        label = item.label,
        stopCountText = item.stopCount.toString(),
        phoneText = truncate(item.phone, 120),
        pinned = "${item.pinned}",
        updatedAt = item.updatedAt.toString(),
        visible = "${item.visible}",
        thresholdText = "%.2f".format(item.threshold),
        unitText = truncate(item.unit, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDutyPeriodBundleViews(items: MutableList<LenientDutyPeriodBundle>): MutableList<LenientDutyPeriodBundleView> {
    val views = mutableListOf<LenientDutyPeriodBundleView>()
    for (item in items) {
        views.add(toLenientDutyPeriodBundleView(item))
    }
    return views
}

fun sumLenientDutyPeriodBundleView(view: LenientDutyPeriodBundleView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedStopCount.toDouble() + parsedUpdatedAt.toDouble() + parsedThreshold
}
