package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation window. */
data class SortationWindowView(
    var code: String,
    var titleText: String,
    var rate: String,
    var checksum: String,
    var externalRef: String,
    var phoneText: String,
    var updatedAt: String,
    var label: String,
    var channel: String,
    var timezone: String,
    var pinned: String,
    var statusLabel: String,
)

fun toSortationWindowView(item: SortationWindow): SortationWindowView {
    return SortationWindowView(
        code = item.code,
        titleText = truncate(item.title, 64),
        rate = "%.2f".format(item.rate),
        checksum = item.checksum,
        externalRef = item.externalRef,
        phoneText = truncate(item.phone, 120),
        updatedAt = item.updatedAt.toString(),
        label = truncate(item.label, 120),
        channel = item.channel,
        timezone = item.timezone,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationWindowViews(items: MutableList<SortationWindow>): MutableList<SortationWindowView> {
    val views = mutableListOf<SortationWindowView>()
    for (item in items) {
        views.add(toSortationWindowView(item))
    }
    return views
}

fun sumSortationWindowView(view: SortationWindowView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRate + parsedUpdatedAt.toDouble()
}
