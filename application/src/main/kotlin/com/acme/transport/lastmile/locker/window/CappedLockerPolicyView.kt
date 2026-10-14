package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped locker policy. */
data class CappedLockerPolicyView(
    var codeText: String,
    var name: String,
    var startsAtText: String,
    var sourceText: String,
    var retries: String,
    var ownerIdText: String,
    var lane: String,
    var statusLabel: String,
)

fun toCappedLockerPolicyView(item: CappedLockerPolicy): CappedLockerPolicyView {
    return CappedLockerPolicyView(
        codeText = item.code,
        name = truncate(item.name, 40),
        startsAtText = item.startsAt.toString(),
        sourceText = item.source,
        retries = item.retries.toString(),
        ownerIdText = truncate(item.ownerId, 40),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLockerPolicyViews(items: MutableList<CappedLockerPolicy>): MutableList<CappedLockerPolicyView> {
    val views = mutableListOf<CappedLockerPolicyView>()
    for (item in items) {
        views.add(toCappedLockerPolicyView(item))
    }
    return views
}

fun sumCappedLockerPolicyView(view: CappedLockerPolicyView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedStartsAt.toDouble() + parsedRetries.toDouble()
}
