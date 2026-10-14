package com.acme.transport.freightpay.accessorial.budget

/** How screens and exports show a accessorial checkpoint. */
data class AccessorialCheckpointView(
    var code: String,
    var name: String,
    var ceiling: String,
    var version: String,
    var active: String,
    var statusLabel: String,
)

fun toAccessorialCheckpointView(item: AccessorialCheckpoint): AccessorialCheckpointView {
    return AccessorialCheckpointView(
        code = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        version = item.version.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialCheckpointViews(items: MutableList<AccessorialCheckpoint>): MutableList<AccessorialCheckpointView> {
    val views = mutableListOf<AccessorialCheckpointView>()
    for (item in items) {
        views.add(toAccessorialCheckpointView(item))
    }
    return views
}

fun sumAccessorialCheckpointView(view: AccessorialCheckpointView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedCeiling + parsedVersion.toDouble()
}
