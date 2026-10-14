package com.acme.transport.safety.nearmiss.schedule

/** How screens and exports show a tiered near miss profile. */
data class TieredNearMissProfileView(
    var code: String,
    var labelText: String,
    var retries: String,
    var startsAt: String,
    var updatedAt: String,
    var pinnedText: String,
    var weight: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTieredNearMissProfileView(item: TieredNearMissProfile): TieredNearMissProfileView {
    return TieredNearMissProfileView(
        code = item.code,
        labelText = item.label,
        retries = item.retries.toString(),
        startsAt = item.startsAt.toString(),
        updatedAt = item.updatedAt.toString(),
        pinnedText = "${item.pinned}",
        weight = "%.2f".format(item.weight),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredNearMissProfileViews(items: MutableList<TieredNearMissProfile>): MutableList<TieredNearMissProfileView> {
    val views = mutableListOf<TieredNearMissProfileView>()
    for (item in items) {
        views.add(toTieredNearMissProfileView(item))
    }
    return views
}

fun sumTieredNearMissProfileView(view: TieredNearMissProfileView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRetries.toDouble() + parsedStartsAt.toDouble() + parsedUpdatedAt.toDouble()
}
