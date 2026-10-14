package com.acme.transport.linehaul.capacity.preference

/** How screens and exports show a fallback capacity preference. */
data class FallbackCapacityPreferenceView(
    var code: String,
    var title: String,
    var startsAtText: String,
    var capacity: String,
    var endsAt: String,
    var priority: String,
    var durationMinutesText: String,
    var routeCodeText: String,
    var createdAt: String,
    var depot: String,
    var statusLabel: String,
)

fun toFallbackCapacityPreferenceView(item: FallbackCapacityPreference): FallbackCapacityPreferenceView {
    return FallbackCapacityPreferenceView(
        code = item.code,
        title = item.title,
        startsAtText = item.startsAt.toString(),
        capacity = item.capacity.toString(),
        endsAt = item.endsAt.toString(),
        priority = item.priority.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        routeCodeText = item.routeCode,
        createdAt = item.createdAt.toString(),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCapacityPreferenceViews(items: MutableList<FallbackCapacityPreference>): MutableList<FallbackCapacityPreferenceView> {
    val views = mutableListOf<FallbackCapacityPreferenceView>()
    for (item in items) {
        views.add(toFallbackCapacityPreferenceView(item))
    }
    return views
}

fun sumFallbackCapacityPreferenceView(view: FallbackCapacityPreferenceView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedStartsAt.toDouble() + parsedCapacity.toDouble() + parsedEndsAt.toDouble()
}
