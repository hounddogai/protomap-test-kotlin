package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global accessorial target. */
data class GlobalAccessorialTargetView(
    var code: String,
    var name: String,
    var source: String,
    var title: String,
    var notes: String,
    var reviewer: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toGlobalAccessorialTargetView(item: GlobalAccessorialTarget): GlobalAccessorialTargetView {
    return GlobalAccessorialTargetView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 64),
        source = item.source,
        title = truncate(item.title, 120),
        notes = item.notes,
        reviewer = item.reviewer,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAccessorialTargetViews(items: MutableList<GlobalAccessorialTarget>): MutableList<GlobalAccessorialTargetView> {
    val views = mutableListOf<GlobalAccessorialTargetView>()
    for (item in items) {
        views.add(toGlobalAccessorialTargetView(item))
    }
    return views
}

fun sumGlobalAccessorialTargetView(view: GlobalAccessorialTargetView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
