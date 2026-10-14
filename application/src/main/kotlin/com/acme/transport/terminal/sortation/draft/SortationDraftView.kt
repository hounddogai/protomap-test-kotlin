package com.acme.transport.terminal.sortation.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation draft. */
data class SortationDraftView(
    var code: String,
    var name: String,
    var phone: String,
    var country: String,
    var categoryText: String,
    var retriesText: String,
    var rate: String,
    var channel: String,
    var localeText: String,
    var revisionText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toSortationDraftView(item: SortationDraft): SortationDraftView {
    return SortationDraftView(
        code = item.code,
        name = item.name,
        phone = item.phone,
        country = item.country,
        categoryText = item.category,
        retriesText = item.retries.toString(),
        rate = "%.2f".format(item.rate),
        channel = item.channel,
        localeText = truncate(item.locale, 120),
        revisionText = item.revision.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationDraftViews(items: MutableList<SortationDraft>): MutableList<SortationDraftView> {
    val views = mutableListOf<SortationDraftView>()
    for (item in items) {
        views.add(toSortationDraftView(item))
    }
    return views
}

fun sumSortationDraftView(view: SortationDraftView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedRetries.toDouble() + parsedRate + parsedRevision.toDouble()
}
