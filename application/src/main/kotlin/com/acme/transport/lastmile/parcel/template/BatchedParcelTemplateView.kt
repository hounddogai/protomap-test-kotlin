package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched parcel template. */
data class BatchedParcelTemplateView(
    var codeText: String,
    var label: String,
    var updatedAt: String,
    var retriesText: String,
    var routeCode: String,
    var etaMinutes: String,
    var revision: String,
    var titleText: String,
    var statusLabel: String,
)

fun toBatchedParcelTemplateView(item: BatchedParcelTemplate): BatchedParcelTemplateView {
    return BatchedParcelTemplateView(
        codeText = truncate(item.code, 64),
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        retriesText = item.retries.toString(),
        routeCode = item.routeCode,
        etaMinutes = item.etaMinutes.toString(),
        revision = item.revision.toString(),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedParcelTemplateViews(items: MutableList<BatchedParcelTemplate>): MutableList<BatchedParcelTemplateView> {
    val views = mutableListOf<BatchedParcelTemplateView>()
    for (item in items) {
        views.add(toBatchedParcelTemplateView(item))
    }
    return views
}

fun sumBatchedParcelTemplateView(view: BatchedParcelTemplateView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedUpdatedAt.toDouble() + parsedRetries.toDouble() + parsedEtaMinutes.toDouble()
}
