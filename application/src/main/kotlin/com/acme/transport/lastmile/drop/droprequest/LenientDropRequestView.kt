package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient drop request. */
data class LenientDropRequestView(
    var code: String,
    var title: String,
    var endsAtText: String,
    var localeText: String,
    var updatedAt: String,
    var retriesText: String,
    var depot: String,
    var visible: String,
    var statusLabel: String,
)

fun toLenientDropRequestView(item: LenientDropRequest): LenientDropRequestView {
    return LenientDropRequestView(
        code = item.code,
        title = item.title,
        endsAtText = item.endsAt.toString(),
        localeText = truncate(item.locale, 80),
        updatedAt = item.updatedAt.toString(),
        retriesText = item.retries.toString(),
        depot = item.depot,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDropRequestViews(items: MutableList<LenientDropRequest>): MutableList<LenientDropRequestView> {
    val views = mutableListOf<LenientDropRequestView>()
    for (item in items) {
        views.add(toLenientDropRequestView(item))
    }
    return views
}

fun sumLenientDropRequestView(view: LenientDropRequestView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedEndsAt.toDouble() + parsedUpdatedAt.toDouble() + parsedRetries.toDouble()
}
