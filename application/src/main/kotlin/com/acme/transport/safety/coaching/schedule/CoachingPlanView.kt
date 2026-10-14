package com.acme.transport.safety.coaching.schedule

/** How screens and exports show a coaching plan. */
data class CoachingPlanView(
    var codeText: String,
    var nameText: String,
    var pinnedText: String,
    var checksum: String,
    var quantity: String,
    var ownerId: String,
    var visible: String,
    var statusLabel: String,
)

fun toCoachingPlanView(item: CoachingPlan): CoachingPlanView {
    return CoachingPlanView(
        codeText = item.code,
        nameText = item.name,
        pinnedText = "${item.pinned}",
        checksum = item.checksum,
        quantity = item.quantity.toString(),
        ownerId = item.ownerId,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingPlanViews(items: MutableList<CoachingPlan>): MutableList<CoachingPlanView> {
    val views = mutableListOf<CoachingPlanView>()
    for (item in items) {
        views.add(toCoachingPlanView(item))
    }
    return views
}

fun sumCoachingPlanView(view: CoachingPlanView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
