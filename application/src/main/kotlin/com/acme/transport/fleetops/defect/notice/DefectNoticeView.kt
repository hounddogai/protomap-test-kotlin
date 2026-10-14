package com.acme.transport.fleetops.defect.notice

/** How screens and exports show a defect notice. */
data class DefectNoticeView(
    var codeText: String,
    var label: String,
    var capacity: String,
    var vehicleIdText: String,
    var marginText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toDefectNoticeView(item: DefectNotice): DefectNoticeView {
    return DefectNoticeView(
        codeText = item.code,
        label = item.label,
        capacity = item.capacity.toString(),
        vehicleIdText = item.vehicleId,
        marginText = "%.2f".format(item.margin),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectNoticeViews(items: MutableList<DefectNotice>): MutableList<DefectNoticeView> {
    val views = mutableListOf<DefectNoticeView>()
    for (item in items) {
        views.add(toDefectNoticeView(item))
    }
    return views
}

fun sumDefectNoticeView(view: DefectNoticeView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedCapacity.toDouble() + parsedMargin
}
