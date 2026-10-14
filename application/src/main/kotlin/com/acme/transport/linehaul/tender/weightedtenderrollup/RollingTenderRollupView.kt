package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tender rollup. */
data class RollingTenderRollupView(
    var code: String,
    var title: String,
    var expiresAtText: String,
    var revisionText: String,
    var dock: String,
    var phone: String,
    var statusLabel: String,
)

fun toRollingTenderRollupView(item: RollingTenderRollup): RollingTenderRollupView {
    return RollingTenderRollupView(
        code = item.code,
        title = item.title,
        expiresAtText = item.expiresAt.toString(),
        revisionText = item.revision.toString(),
        dock = truncate(item.dock, 40),
        phone = truncate(item.phone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTenderRollupViews(items: MutableList<RollingTenderRollup>): MutableList<RollingTenderRollupView> {
    val views = mutableListOf<RollingTenderRollupView>()
    for (item in items) {
        views.add(toRollingTenderRollupView(item))
    }
    return views
}

fun sumRollingTenderRollupView(view: RollingTenderRollupView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedExpiresAt.toDouble() + parsedRevision.toDouble()
}
