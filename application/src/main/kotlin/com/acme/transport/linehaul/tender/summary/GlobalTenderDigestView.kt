package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tender digest. */
data class GlobalTenderDigestView(
    var codeText: String,
    var titleText: String,
    var notesText: String,
    var category: String,
    var revision: String,
    var activeText: String,
    var reviewer: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toGlobalTenderDigestView(item: GlobalTenderDigest): GlobalTenderDigestView {
    return GlobalTenderDigestView(
        codeText = item.code,
        titleText = item.title,
        notesText = item.notes,
        category = truncate(item.category, 120),
        revision = item.revision.toString(),
        activeText = "${item.active}",
        reviewer = item.reviewer,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderDigestViews(items: MutableList<GlobalTenderDigest>): MutableList<GlobalTenderDigestView> {
    val views = mutableListOf<GlobalTenderDigestView>()
    for (item in items) {
        views.add(toGlobalTenderDigestView(item))
    }
    return views
}

fun sumGlobalTenderDigestView(view: GlobalTenderDigestView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRevision.toDouble() + parsedStartsAt.toDouble()
}
