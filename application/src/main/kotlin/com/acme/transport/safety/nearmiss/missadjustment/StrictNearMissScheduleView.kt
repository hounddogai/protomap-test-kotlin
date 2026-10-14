package com.acme.transport.safety.nearmiss.missadjustment

/** How screens and exports show a strict near miss schedule. */
data class StrictNearMissScheduleView(
    var code: String,
    var name: String,
    var channelText: String,
    var endsAt: String,
    var visible: String,
    var category: String,
    var createdAt: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toStrictNearMissScheduleView(item: StrictNearMissSchedule): StrictNearMissScheduleView {
    return StrictNearMissScheduleView(
        code = item.code,
        name = item.name,
        channelText = item.channel,
        endsAt = item.endsAt.toString(),
        visible = "${item.visible}",
        category = item.category,
        createdAt = item.createdAt.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictNearMissScheduleViews(items: MutableList<StrictNearMissSchedule>): MutableList<StrictNearMissScheduleView> {
    val views = mutableListOf<StrictNearMissScheduleView>()
    for (item in items) {
        views.add(toStrictNearMissScheduleView(item))
    }
    return views
}

fun sumStrictNearMissScheduleView(view: StrictNearMissScheduleView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble()
}
