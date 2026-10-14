package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer target. */
data class ManualTrailerTargetView(
    var codeText: String,
    var name: String,
    var approved: String,
    var rate: String,
    var budget: String,
    var priority: String,
    var localeText: String,
    var statusLabel: String,
)

fun toManualTrailerTargetView(item: ManualTrailerTarget): ManualTrailerTargetView {
    return ManualTrailerTargetView(
        codeText = item.code,
        name = truncate(item.name, 40),
        approved = "${item.approved}",
        rate = "%.2f".format(item.rate),
        budget = "%.2f".format(item.budget),
        priority = item.priority.toString(),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerTargetViews(items: MutableList<ManualTrailerTarget>): MutableList<ManualTrailerTargetView> {
    val views = mutableListOf<ManualTrailerTargetView>()
    for (item in items) {
        views.add(toManualTrailerTargetView(item))
    }
    return views
}

fun sumManualTrailerTargetView(view: ManualTrailerTargetView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedRate + parsedBudget + parsedPriority.toDouble()
}
