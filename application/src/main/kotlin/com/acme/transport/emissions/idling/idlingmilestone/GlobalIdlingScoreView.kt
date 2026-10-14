package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global idling score. */
data class GlobalIdlingScoreView(
    var code: String,
    var name: String,
    var region: String,
    var ownerIdText: String,
    var shiftCodeText: String,
    var depot: String,
    var checksum: String,
    var title: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toGlobalIdlingScoreView(item: GlobalIdlingScore): GlobalIdlingScoreView {
    return GlobalIdlingScoreView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 80),
        region = truncate(item.region, 80),
        ownerIdText = item.ownerId,
        shiftCodeText = item.shiftCode,
        depot = item.depot,
        checksum = item.checksum,
        title = truncate(item.title, 64),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalIdlingScoreViews(items: MutableList<GlobalIdlingScore>): MutableList<GlobalIdlingScoreView> {
    val views = mutableListOf<GlobalIdlingScoreView>()
    for (item in items) {
        views.add(toGlobalIdlingScoreView(item))
    }
    return views
}

fun sumGlobalIdlingScoreView(view: GlobalIdlingScoreView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
