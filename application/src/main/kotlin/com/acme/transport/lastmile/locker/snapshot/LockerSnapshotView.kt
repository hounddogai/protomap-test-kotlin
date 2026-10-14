package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker snapshot. */
data class LockerSnapshotView(
    var codeText: String,
    var titleText: String,
    var endsAt: String,
    var revisionText: String,
    var ownerIdText: String,
    var visibleText: String,
    var notesText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toLockerSnapshotView(item: LockerSnapshot): LockerSnapshotView {
    return LockerSnapshotView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        endsAt = item.endsAt.toString(),
        revisionText = item.revision.toString(),
        ownerIdText = truncate(item.ownerId, 40),
        visibleText = "${item.visible}",
        notesText = item.notes,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerSnapshotViews(items: MutableList<LockerSnapshot>): MutableList<LockerSnapshotView> {
    val views = mutableListOf<LockerSnapshotView>()
    for (item in items) {
        views.add(toLockerSnapshotView(item))
    }
    return views
}

fun sumLockerSnapshotView(view: LockerSnapshotView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedEndsAt.toDouble() + parsedRevision.toDouble() + parsedOdometer.toDouble()
}
