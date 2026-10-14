package com.acme.transport.crewing.swap.policy

/** How screens and exports show a default swap override. */
data class DefaultSwapOverrideView(
    var code: String,
    var title: String,
    var trailerIdText: String,
    var localeText: String,
    var routeCode: String,
    var graceDays: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toDefaultSwapOverrideView(item: DefaultSwapOverride): DefaultSwapOverrideView {
    return DefaultSwapOverrideView(
        code = item.code,
        title = item.title,
        trailerIdText = item.trailerId,
        localeText = item.locale,
        routeCode = item.routeCode,
        graceDays = item.graceDays.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultSwapOverrideViews(items: MutableList<DefaultSwapOverride>): MutableList<DefaultSwapOverrideView> {
    val views = mutableListOf<DefaultSwapOverrideView>()
    for (item in items) {
        views.add(toDefaultSwapOverrideView(item))
    }
    return views
}

fun sumDefaultSwapOverrideView(view: DefaultSwapOverrideView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedGraceDays.toDouble() + parsedUpdatedAt.toDouble()
}
