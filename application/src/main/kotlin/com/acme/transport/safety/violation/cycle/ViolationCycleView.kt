package com.acme.transport.safety.violation.cycle

/** How screens and exports show a violation cycle. */
data class ViolationCycleView(
    var code: String,
    var name: String,
    var windowDays: String,
    var active: String,
    var archived: String,
    var statusLabel: String,
)

fun toViolationCycleView(item: ViolationCycle): ViolationCycleView {
    return ViolationCycleView(
        code = item.code,
        name = item.name,
        windowDays = item.windowDays.toString(),
        active = "${item.active}",
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationCycleViews(items: MutableList<ViolationCycle>): MutableList<ViolationCycleView> {
    val views = mutableListOf<ViolationCycleView>()
    for (item in items) {
        views.add(toViolationCycleView(item))
    }
    return views
}

fun sumViolationCycleView(view: ViolationCycleView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWindowDays.toDouble()
}
