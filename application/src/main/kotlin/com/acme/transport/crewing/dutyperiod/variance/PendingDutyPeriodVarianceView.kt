package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending duty period variance. */
data class PendingDutyPeriodVarianceView(
    var codeText: String,
    var titleText: String,
    var routeCode: String,
    var visibleText: String,
    var currencyText: String,
    var score: String,
    var ratioText: String,
    var batchSizeText: String,
    var label: String,
    var locale: String,
    var description: String,
    var statusLabel: String,
)

fun toPendingDutyPeriodVarianceView(item: PendingDutyPeriodVariance): PendingDutyPeriodVarianceView {
    return PendingDutyPeriodVarianceView(
        codeText = item.code,
        titleText = item.title,
        routeCode = item.routeCode,
        visibleText = "${item.visible}",
        currencyText = truncate(item.currency, 120),
        score = "%.2f".format(item.score),
        ratioText = "%.2f".format(item.ratio),
        batchSizeText = item.batchSize.toString(),
        label = truncate(item.label, 120),
        locale = item.locale,
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDutyPeriodVarianceViews(items: MutableList<PendingDutyPeriodVariance>): MutableList<PendingDutyPeriodVarianceView> {
    val views = mutableListOf<PendingDutyPeriodVarianceView>()
    for (item in items) {
        views.add(toPendingDutyPeriodVarianceView(item))
    }
    return views
}

fun sumPendingDutyPeriodVarianceView(view: PendingDutyPeriodVarianceView): Double {
    val parsedScore = view.score.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedScore + parsedRatio + parsedBatchSize.toDouble()
}
