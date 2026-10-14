package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification snapshot. */
data class QualificationSnapshotView(
    var codeText: String,
    var name: String,
    var createdAtText: String,
    var timezone: String,
    var costText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toQualificationSnapshotView(item: QualificationSnapshot): QualificationSnapshotView {
    return QualificationSnapshotView(
        codeText = item.code,
        name = item.name,
        createdAtText = item.createdAt.toString(),
        timezone = truncate(item.timezone, 64),
        costText = "%.2f".format(item.cost),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationSnapshotViews(items: MutableList<QualificationSnapshot>): MutableList<QualificationSnapshotView> {
    val views = mutableListOf<QualificationSnapshotView>()
    for (item in items) {
        views.add(toQualificationSnapshotView(item))
    }
    return views
}

fun sumQualificationSnapshotView(view: QualificationSnapshotView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedCreatedAt.toDouble() + parsedCost + parsedEndsAt.toDouble()
}
