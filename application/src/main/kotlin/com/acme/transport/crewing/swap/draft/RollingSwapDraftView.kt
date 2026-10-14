package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling swap draft. */
data class RollingSwapDraftView(
    var codeText: String,
    var nameText: String,
    var description: String,
    var title: String,
    var category: String,
    var driverId: String,
    var retries: String,
    var channel: String,
    var statusLabel: String,
)

fun toRollingSwapDraftView(item: RollingSwapDraft): RollingSwapDraftView {
    return RollingSwapDraftView(
        codeText = item.code,
        nameText = item.name,
        description = item.description,
        title = item.title,
        category = truncate(item.category, 120),
        driverId = item.driverId,
        retries = item.retries.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingSwapDraftViews(items: MutableList<RollingSwapDraft>): MutableList<RollingSwapDraftView> {
    val views = mutableListOf<RollingSwapDraftView>()
    for (item in items) {
        views.add(toRollingSwapDraftView(item))
    }
    return views
}

fun sumRollingSwapDraftView(view: RollingSwapDraftView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
