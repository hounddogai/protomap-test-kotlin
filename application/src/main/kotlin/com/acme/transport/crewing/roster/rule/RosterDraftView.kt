package com.acme.transport.crewing.roster.rule

/** How screens and exports show a roster draft. */
data class RosterDraftView(
    var code: String,
    var label: String,
    var approved: String,
    var routeCodeText: String,
    var carrier: String,
    var updatedAtText: String,
    var archivedText: String,
    var expiresAt: String,
    var minimumText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toRosterDraftView(item: RosterDraft): RosterDraftView {
    return RosterDraftView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        routeCodeText = item.routeCode,
        carrier = item.carrier,
        updatedAtText = item.updatedAt.toString(),
        archivedText = "${item.archived}",
        expiresAt = item.expiresAt.toString(),
        minimumText = "%.2f".format(item.minimum),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterDraftViews(items: MutableList<RosterDraft>): MutableList<RosterDraftView> {
    val views = mutableListOf<RosterDraftView>()
    for (item in items) {
        views.add(toRosterDraftView(item))
    }
    return views
}

fun sumRosterDraftView(view: RosterDraftView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble() + parsedMinimum
}
