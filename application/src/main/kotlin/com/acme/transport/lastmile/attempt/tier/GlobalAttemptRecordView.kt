package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global attempt record. */
data class GlobalAttemptRecordView(
    var codeText: String,
    var labelText: String,
    var reviewer: String,
    var revision: String,
    var source: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toGlobalAttemptRecordView(item: GlobalAttemptRecord): GlobalAttemptRecordView {
    return GlobalAttemptRecordView(
        codeText = item.code,
        labelText = item.label,
        reviewer = truncate(item.reviewer, 40),
        revision = item.revision.toString(),
        source = item.source,
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAttemptRecordViews(items: MutableList<GlobalAttemptRecord>): MutableList<GlobalAttemptRecordView> {
    val views = mutableListOf<GlobalAttemptRecordView>()
    for (item in items) {
        views.add(toGlobalAttemptRecordView(item))
    }
    return views
}

fun sumGlobalAttemptRecordView(view: GlobalAttemptRecordView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
