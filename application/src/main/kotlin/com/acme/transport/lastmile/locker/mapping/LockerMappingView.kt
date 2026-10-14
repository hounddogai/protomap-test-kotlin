package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker mapping. */
data class LockerMappingView(
    var codeText: String,
    var label: String,
    var weightText: String,
    var externalRef: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toLockerMappingView(item: LockerMapping): LockerMappingView {
    return LockerMappingView(
        codeText = item.code,
        label = item.label,
        weightText = "%.2f".format(item.weight),
        externalRef = truncate(item.externalRef, 64),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerMappingViews(items: MutableList<LockerMapping>): MutableList<LockerMappingView> {
    val views = mutableListOf<LockerMappingView>()
    for (item in items) {
        views.add(toLockerMappingView(item))
    }
    return views
}

fun sumLockerMappingView(view: LockerMappingView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedWeight + parsedCreatedAt.toDouble()
}
