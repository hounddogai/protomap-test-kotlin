package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling node draft. */
data class RollingNodeDraftView(
    var code: String,
    var titleText: String,
    var trailerId: String,
    var routeCodeText: String,
    var notes: String,
    var amountText: String,
    var statusLabel: String,
)

fun toRollingNodeDraftView(item: RollingNodeDraft): RollingNodeDraftView {
    return RollingNodeDraftView(
        code = item.code,
        titleText = truncate(item.title, 40),
        trailerId = item.trailerId,
        routeCodeText = truncate(item.routeCode, 120),
        notes = truncate(item.notes, 120),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingNodeDraftViews(items: MutableList<RollingNodeDraft>): MutableList<RollingNodeDraftView> {
    val views = mutableListOf<RollingNodeDraftView>()
    for (item in items) {
        views.add(toRollingNodeDraftView(item))
    }
    return views
}

fun sumRollingNodeDraftView(view: RollingNodeDraftView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
