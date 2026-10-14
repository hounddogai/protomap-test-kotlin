package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global defect profile. */
data class GlobalDefectProfileView(
    var code: String,
    var name: String,
    var endsAt: String,
    var revision: String,
    var quantity: String,
    var contactEmail: String,
    var trailerId: String,
    var notes: String,
    var priority: String,
    var stopCount: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toGlobalDefectProfileView(item: GlobalDefectProfile): GlobalDefectProfileView {
    return GlobalDefectProfileView(
        code = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        revision = item.revision.toString(),
        quantity = item.quantity.toString(),
        contactEmail = truncate(item.contactEmail, 120),
        trailerId = item.trailerId,
        notes = item.notes,
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDefectProfileViews(items: MutableList<GlobalDefectProfile>): MutableList<GlobalDefectProfileView> {
    val views = mutableListOf<GlobalDefectProfileView>()
    for (item in items) {
        views.add(toGlobalDefectProfileView(item))
    }
    return views
}

fun sumGlobalDefectProfileView(view: GlobalDefectProfileView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedEndsAt.toDouble() + parsedRevision.toDouble() + parsedQuantity.toDouble()
}
