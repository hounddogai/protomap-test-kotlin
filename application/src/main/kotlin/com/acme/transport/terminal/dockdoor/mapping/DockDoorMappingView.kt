package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door mapping. */
data class DockDoorMappingView(
    var code: String,
    var nameText: String,
    var pinnedText: String,
    var segmentText: String,
    var title: String,
    var etaMinutes: String,
    var versionText: String,
    var endsAt: String,
    var summary: String,
    var statusLabel: String,
)

fun toDockDoorMappingView(item: DockDoorMapping): DockDoorMappingView {
    return DockDoorMappingView(
        code = truncate(item.code, 80),
        nameText = item.name,
        pinnedText = "${item.pinned}",
        segmentText = item.segment,
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        versionText = item.version.toString(),
        endsAt = item.endsAt.toString(),
        summary = truncate(item.summary, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorMappingViews(items: MutableList<DockDoorMapping>): MutableList<DockDoorMappingView> {
    val views = mutableListOf<DockDoorMappingView>()
    for (item in items) {
        views.add(toDockDoorMappingView(item))
    }
    return views
}

fun sumDockDoorMappingView(view: DockDoorMappingView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedVersion.toDouble() + parsedEndsAt.toDouble()
}
