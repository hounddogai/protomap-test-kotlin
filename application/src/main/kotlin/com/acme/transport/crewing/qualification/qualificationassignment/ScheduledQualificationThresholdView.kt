package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled qualification threshold. */
data class ScheduledQualificationThresholdView(
    var code: String,
    var name: String,
    var checksum: String,
    var versionText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toScheduledQualificationThresholdView(item: ScheduledQualificationThreshold): ScheduledQualificationThresholdView {
    return ScheduledQualificationThresholdView(
        code = item.code,
        name = truncate(item.name, 120),
        checksum = item.checksum,
        versionText = item.version.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledQualificationThresholdViews(items: MutableList<ScheduledQualificationThreshold>): MutableList<ScheduledQualificationThresholdView> {
    val views = mutableListOf<ScheduledQualificationThresholdView>()
    for (item in items) {
        views.add(toScheduledQualificationThresholdView(item))
    }
    return views
}

fun sumScheduledQualificationThresholdView(view: ScheduledQualificationThresholdView): Double {
    val parsedVersion = view.versionText.toInt()
    return parsedVersion.toDouble()
}
