package com.acme.transport.lastmile.drop.bundle

/** How screens and exports show a drop tier. */
data class DropTierView(
    var code: String,
    var labelText: String,
    var attempts: String,
    var limit: String,
    var updatedAt: String,
    var minimum: String,
    var timezoneText: String,
    var distanceKm: String,
    var ratio: String,
    var score: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toDropTierView(item: DropTier): DropTierView {
    return DropTierView(
        code = item.code,
        labelText = item.label,
        attempts = item.attempts.toString(),
        limit = item.limit.toString(),
        updatedAt = item.updatedAt.toString(),
        minimum = "%.2f".format(item.minimum),
        timezoneText = item.timezone,
        distanceKm = "%.2f".format(item.distanceKm),
        ratio = "%.2f".format(item.ratio),
        score = "%.2f".format(item.score),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropTierViews(items: MutableList<DropTier>): MutableList<DropTierView> {
    val views = mutableListOf<DropTierView>()
    for (item in items) {
        views.add(toDropTierView(item))
    }
    return views
}

fun sumDropTierView(view: DropTierView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedAttempts.toDouble() + parsedLimit.toDouble() + parsedUpdatedAt.toDouble()
}
