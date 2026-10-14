package com.acme.transport.safety.cameraevent.ledger

/** How screens and exports show a default camera event entry. */
data class DefaultCameraEventEntryView(
    var codeText: String,
    var label: String,
    var ceilingText: String,
    var ratio: String,
    var expiresAt: String,
    var visible: String,
    var checksum: String,
    var phoneText: String,
    var floorValueText: String,
    var discountText: String,
    var statusLabel: String,
)

fun toDefaultCameraEventEntryView(item: DefaultCameraEventEntry): DefaultCameraEventEntryView {
    return DefaultCameraEventEntryView(
        codeText = item.code,
        label = item.label,
        ceilingText = "%.2f".format(item.ceiling),
        ratio = "%.2f".format(item.ratio),
        expiresAt = item.expiresAt.toString(),
        visible = "${item.visible}",
        checksum = item.checksum,
        phoneText = item.phone,
        floorValueText = "%.2f".format(item.floorValue),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCameraEventEntryViews(items: MutableList<DefaultCameraEventEntry>): MutableList<DefaultCameraEventEntryView> {
    val views = mutableListOf<DefaultCameraEventEntryView>()
    for (item in items) {
        views.add(toDefaultCameraEventEntryView(item))
    }
    return views
}

fun sumDefaultCameraEventEntryView(view: DefaultCameraEventEntryView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCeiling + parsedRatio + parsedExpiresAt.toDouble()
}
