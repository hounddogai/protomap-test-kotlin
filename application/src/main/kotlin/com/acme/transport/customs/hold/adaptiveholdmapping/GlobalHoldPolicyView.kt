package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global hold policy. */
data class GlobalHoldPolicyView(
    var codeText: String,
    var name: String,
    var floorValue: String,
    var depotText: String,
    var graceDays: String,
    var notesText: String,
    var checksum: String,
    var versionText: String,
    var statusLabel: String,
)

fun toGlobalHoldPolicyView(item: GlobalHoldPolicy): GlobalHoldPolicyView {
    return GlobalHoldPolicyView(
        codeText = item.code,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        depotText = item.depot,
        graceDays = item.graceDays.toString(),
        notesText = truncate(item.notes, 80),
        checksum = item.checksum,
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalHoldPolicyViews(items: MutableList<GlobalHoldPolicy>): MutableList<GlobalHoldPolicyView> {
    val views = mutableListOf<GlobalHoldPolicyView>()
    for (item in items) {
        views.add(toGlobalHoldPolicyView(item))
    }
    return views
}

fun sumGlobalHoldPolicyView(view: GlobalHoldPolicyView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedFloorValue + parsedGraceDays.toDouble() + parsedVersion.toDouble()
}
