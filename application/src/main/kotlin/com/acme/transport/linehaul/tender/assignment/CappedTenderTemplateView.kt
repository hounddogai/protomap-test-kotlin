package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped tender template. */
data class CappedTenderTemplateView(
    var code: String,
    var label: String,
    var version: String,
    var ownerIdText: String,
    var categoryText: String,
    var dock: String,
    var routeCode: String,
    var minimum: String,
    var statusLabel: String,
)

fun toCappedTenderTemplateView(item: CappedTenderTemplate): CappedTenderTemplateView {
    return CappedTenderTemplateView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        version = item.version.toString(),
        ownerIdText = item.ownerId,
        categoryText = item.category,
        dock = truncate(item.dock, 40),
        routeCode = item.routeCode,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTenderTemplateViews(items: MutableList<CappedTenderTemplate>): MutableList<CappedTenderTemplateView> {
    val views = mutableListOf<CappedTenderTemplateView>()
    for (item in items) {
        views.add(toCappedTenderTemplateView(item))
    }
    return views
}

fun sumCappedTenderTemplateView(view: CappedTenderTemplateView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedVersion.toDouble() + parsedMinimum
}
