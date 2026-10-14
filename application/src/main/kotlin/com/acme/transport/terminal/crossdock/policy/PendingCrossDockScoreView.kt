package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending cross dock score. */
data class PendingCrossDockScoreView(
    var codeText: String,
    var titleText: String,
    var revision: String,
    var updatedAt: String,
    var version: String,
    var statusLabel: String,
)

fun toPendingCrossDockScoreView(item: PendingCrossDockScore): PendingCrossDockScoreView {
    return PendingCrossDockScoreView(
        codeText = item.code,
        titleText = truncate(item.title, 64),
        revision = item.revision.toString(),
        updatedAt = item.updatedAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCrossDockScoreViews(items: MutableList<PendingCrossDockScore>): MutableList<PendingCrossDockScoreView> {
    val views = mutableListOf<PendingCrossDockScoreView>()
    for (item in items) {
        views.add(toPendingCrossDockScoreView(item))
    }
    return views
}

fun sumPendingCrossDockScoreView(view: PendingCrossDockScoreView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedVersion = view.version.toInt()
    return parsedRevision.toDouble() + parsedUpdatedAt.toDouble() + parsedVersion.toDouble()
}
