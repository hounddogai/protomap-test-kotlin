package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification milestone. */
data class QualificationMilestoneView(
    var code: String,
    var nameText: String,
    var weightText: String,
    var lane: String,
    var endsAt: String,
    var capacity: String,
    var title: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toQualificationMilestoneView(item: QualificationMilestone): QualificationMilestoneView {
    return QualificationMilestoneView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 64),
        weightText = "%.2f".format(item.weight),
        lane = item.lane,
        endsAt = item.endsAt.toString(),
        capacity = item.capacity.toString(),
        title = truncate(item.title, 80),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationMilestoneViews(items: MutableList<QualificationMilestone>): MutableList<QualificationMilestoneView> {
    val views = mutableListOf<QualificationMilestoneView>()
    for (item in items) {
        views.add(toQualificationMilestoneView(item))
    }
    return views
}

fun sumQualificationMilestoneView(view: QualificationMilestoneView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedWeight + parsedEndsAt.toDouble() + parsedCapacity.toDouble()
}
