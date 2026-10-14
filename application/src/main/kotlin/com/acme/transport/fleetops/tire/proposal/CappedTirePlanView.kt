package com.acme.transport.fleetops.tire.proposal

/** How screens and exports show a capped tire plan. */
data class CappedTirePlanView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var endsAtText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toCappedTirePlanView(item: CappedTirePlan): CappedTirePlanView {
    return CappedTirePlanView(
        code = item.code,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        endsAtText = item.endsAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTirePlanViews(items: MutableList<CappedTirePlan>): MutableList<CappedTirePlanView> {
    val views = mutableListOf<CappedTirePlanView>()
    for (item in items) {
        views.add(toCappedTirePlanView(item))
    }
    return views
}

fun sumCappedTirePlanView(view: CappedTirePlanView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedExpiresAt.toDouble() + parsedEndsAt.toDouble() + parsedWindowDays.toDouble()
}
