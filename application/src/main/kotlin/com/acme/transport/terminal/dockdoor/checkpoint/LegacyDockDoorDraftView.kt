package com.acme.transport.terminal.dockdoor.checkpoint

/** How screens and exports show a legacy dock door draft. */
data class LegacyDockDoorDraftView(
    var code: String,
    var title: String,
    var updatedAtText: String,
    var endsAt: String,
    var descriptionText: String,
    var labelText: String,
    var statusLabel: String,
)

fun toLegacyDockDoorDraftView(item: LegacyDockDoorDraft): LegacyDockDoorDraftView {
    return LegacyDockDoorDraftView(
        code = item.code,
        title = item.title,
        updatedAtText = item.updatedAt.toString(),
        endsAt = item.endsAt.toString(),
        descriptionText = item.description,
        labelText = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDockDoorDraftViews(items: MutableList<LegacyDockDoorDraft>): MutableList<LegacyDockDoorDraftView> {
    val views = mutableListOf<LegacyDockDoorDraftView>()
    for (item in items) {
        views.add(toLegacyDockDoorDraftView(item))
    }
    return views
}

fun sumLegacyDockDoorDraftView(view: LegacyDockDoorDraftView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedUpdatedAt.toDouble() + parsedEndsAt.toDouble()
}
