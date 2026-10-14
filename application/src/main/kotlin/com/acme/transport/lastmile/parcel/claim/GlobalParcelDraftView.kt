package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global parcel draft. */
data class GlobalParcelDraftView(
    var codeText: String,
    var name: String,
    var graceDaysText: String,
    var summary: String,
    var updatedAt: String,
    var currency: String,
    var expiresAt: String,
    var notes: String,
    var statusLabel: String,
)

fun toGlobalParcelDraftView(item: GlobalParcelDraft): GlobalParcelDraftView {
    return GlobalParcelDraftView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 40),
        graceDaysText = item.graceDays.toString(),
        summary = item.summary,
        updatedAt = item.updatedAt.toString(),
        currency = item.currency,
        expiresAt = item.expiresAt.toString(),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalParcelDraftViews(items: MutableList<GlobalParcelDraft>): MutableList<GlobalParcelDraftView> {
    val views = mutableListOf<GlobalParcelDraftView>()
    for (item in items) {
        views.add(toGlobalParcelDraftView(item))
    }
    return views
}

fun sumGlobalParcelDraftView(view: GlobalParcelDraftView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedGraceDays.toDouble() + parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble()
}
