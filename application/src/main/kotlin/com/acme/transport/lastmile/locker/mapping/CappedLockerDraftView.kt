package com.acme.transport.lastmile.locker.mapping

/** How screens and exports show a capped locker draft. */
data class CappedLockerDraftView(
    var code: String,
    var title: String,
    var contactEmail: String,
    var updatedAtText: String,
    var driverId: String,
    var endsAt: String,
    var priorityText: String,
    var notes: String,
    var statusLabel: String,
)

fun toCappedLockerDraftView(item: CappedLockerDraft): CappedLockerDraftView {
    return CappedLockerDraftView(
        code = item.code,
        title = item.title,
        contactEmail = item.contactEmail,
        updatedAtText = item.updatedAt.toString(),
        driverId = item.driverId,
        endsAt = item.endsAt.toString(),
        priorityText = item.priority.toString(),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLockerDraftViews(items: MutableList<CappedLockerDraft>): MutableList<CappedLockerDraftView> {
    val views = mutableListOf<CappedLockerDraftView>()
    for (item in items) {
        views.add(toCappedLockerDraftView(item))
    }
    return views
}

fun sumCappedLockerDraftView(view: CappedLockerDraftView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedUpdatedAt.toDouble() + parsedEndsAt.toDouble() + parsedPriority.toDouble()
}
