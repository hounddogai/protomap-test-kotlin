package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan tier. */
data class LanePlanTierView(
    var code: String,
    var nameText: String,
    var source: String,
    var shiftCodeText: String,
    var contactEmail: String,
    var updatedAtText: String,
    var phone: String,
    var active: String,
    var floorValue: String,
    var score: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toLanePlanTierView(item: LanePlanTier): LanePlanTierView {
    return LanePlanTierView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 40),
        source = truncate(item.source, 64),
        shiftCodeText = item.shiftCode,
        contactEmail = truncate(item.contactEmail, 80),
        updatedAtText = item.updatedAt.toString(),
        phone = item.phone,
        active = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        score = "%.2f".format(item.score),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanTierViews(items: MutableList<LanePlanTier>): MutableList<LanePlanTierView> {
    val views = mutableListOf<LanePlanTierView>()
    for (item in items) {
        views.add(toLanePlanTierView(item))
    }
    return views
}

fun sumLanePlanTierView(view: LanePlanTierView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedUpdatedAt.toDouble() + parsedFloorValue + parsedScore
}
