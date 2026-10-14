package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node snapshot. */
data class NodeSnapshotView(
    var codeText: String,
    var label: String,
    var pinned: String,
    var shiftCodeText: String,
    var checksum: String,
    var pallets: String,
    var statusLabel: String,
)

fun toNodeSnapshotView(item: NodeSnapshot): NodeSnapshotView {
    return NodeSnapshotView(
        codeText = item.code,
        label = truncate(item.label, 80),
        pinned = "${item.pinned}",
        shiftCodeText = item.shiftCode,
        checksum = item.checksum,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeSnapshotViews(items: MutableList<NodeSnapshot>): MutableList<NodeSnapshotView> {
    val views = mutableListOf<NodeSnapshotView>()
    for (item in items) {
        views.add(toNodeSnapshotView(item))
    }
    return views
}

fun sumNodeSnapshotView(view: NodeSnapshotView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
