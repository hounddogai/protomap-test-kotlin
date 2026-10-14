package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched qualification template. */
data class BatchedQualificationTemplateView(
    var code: String,
    var title: String,
    var active: String,
    var endsAt: String,
    var ceilingText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toBatchedQualificationTemplateView(item: BatchedQualificationTemplate): BatchedQualificationTemplateView {
    return BatchedQualificationTemplateView(
        code = item.code,
        title = truncate(item.title, 80),
        active = "${item.active}",
        endsAt = item.endsAt.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedQualificationTemplateViews(items: MutableList<BatchedQualificationTemplate>): MutableList<BatchedQualificationTemplateView> {
    val views = mutableListOf<BatchedQualificationTemplateView>()
    for (item in items) {
        views.add(toBatchedQualificationTemplateView(item))
    }
    return views
}

fun sumBatchedQualificationTemplateView(view: BatchedQualificationTemplateView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedEndsAt.toDouble() + parsedCeiling + parsedMaximum
}
