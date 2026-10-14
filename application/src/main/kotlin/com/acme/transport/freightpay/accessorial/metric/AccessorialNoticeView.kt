package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial notice. */
data class AccessorialNoticeView(
    var codeText: String,
    var label: String,
    var routeCode: String,
    var segment: String,
    var ceiling: String,
    var budgetText: String,
    var maximum: String,
    var ownerId: String,
    var batchSize: String,
    var name: String,
    var statusLabel: String,
)

fun toAccessorialNoticeView(item: AccessorialNotice): AccessorialNoticeView {
    return AccessorialNoticeView(
        codeText = item.code,
        label = item.label,
        routeCode = truncate(item.routeCode, 40),
        segment = item.segment,
        ceiling = "%.2f".format(item.ceiling),
        budgetText = "%.2f".format(item.budget),
        maximum = "%.2f".format(item.maximum),
        ownerId = item.ownerId,
        batchSize = item.batchSize.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialNoticeViews(items: MutableList<AccessorialNotice>): MutableList<AccessorialNoticeView> {
    val views = mutableListOf<AccessorialNoticeView>()
    for (item in items) {
        views.add(toAccessorialNoticeView(item))
    }
    return views
}

fun sumAccessorialNoticeView(view: AccessorialNoticeView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCeiling + parsedBudget + parsedMaximum
}
