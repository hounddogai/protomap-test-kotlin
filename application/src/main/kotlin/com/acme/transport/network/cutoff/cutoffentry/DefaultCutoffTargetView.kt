package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cutoff target. */
data class DefaultCutoffTargetView(
    var code: String,
    var labelText: String,
    var currency: String,
    var priority: String,
    var updatedAt: String,
    var limit: String,
    var threshold: String,
    var depot: String,
    var startsAt: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toDefaultCutoffTargetView(item: DefaultCutoffTarget): DefaultCutoffTargetView {
    return DefaultCutoffTargetView(
        code = truncate(item.code, 120),
        labelText = item.label,
        currency = truncate(item.currency, 40),
        priority = item.priority.toString(),
        updatedAt = item.updatedAt.toString(),
        limit = item.limit.toString(),
        threshold = "%.2f".format(item.threshold),
        depot = truncate(item.depot, 120),
        startsAt = item.startsAt.toString(),
        externalRef = truncate(item.externalRef, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCutoffTargetViews(items: MutableList<DefaultCutoffTarget>): MutableList<DefaultCutoffTargetView> {
    val views = mutableListOf<DefaultCutoffTargetView>()
    for (item in items) {
        views.add(toDefaultCutoffTargetView(item))
    }
    return views
}

fun sumDefaultCutoffTargetView(view: DefaultCutoffTargetView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedPriority.toDouble() + parsedUpdatedAt.toDouble() + parsedLimit.toDouble()
}
