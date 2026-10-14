package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tender manifest. */
data class RollingTenderManifestView(
    var codeText: String,
    var label: String,
    var channel: String,
    var lane: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toRollingTenderManifestView(item: RollingTenderManifest): RollingTenderManifestView {
    return RollingTenderManifestView(
        codeText = truncate(item.code, 64),
        label = item.label,
        channel = truncate(item.channel, 64),
        lane = item.lane,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTenderManifestViews(items: MutableList<RollingTenderManifest>): MutableList<RollingTenderManifestView> {
    val views = mutableListOf<RollingTenderManifestView>()
    for (item in items) {
        views.add(toRollingTenderManifestView(item))
    }
    return views
}

fun sumRollingTenderManifestView(view: RollingTenderManifestView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedUpdatedAt.toDouble()
}
