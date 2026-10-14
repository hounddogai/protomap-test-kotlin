package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard draft. */
data class TrailerYardDraftView(
    var codeText: String,
    var title: String,
    var phone: String,
    var minimum: String,
    var statusLabel: String,
)

fun toTrailerYardDraftView(item: TrailerYardDraft): TrailerYardDraftView {
    return TrailerYardDraftView(
        codeText = truncate(item.code, 64),
        title = item.title,
        phone = truncate(item.phone, 40),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardDraftViews(items: MutableList<TrailerYardDraft>): MutableList<TrailerYardDraftView> {
    val views = mutableListOf<TrailerYardDraftView>()
    for (item in items) {
        views.add(toTrailerYardDraftView(item))
    }
    return views
}

fun sumTrailerYardDraftView(view: TrailerYardDraftView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
