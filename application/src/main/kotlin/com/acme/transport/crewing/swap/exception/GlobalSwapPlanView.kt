package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global swap plan. */
data class GlobalSwapPlanView(
    var code: String,
    var name: String,
    var attempts: String,
    var retries: String,
    var segment: String,
    var locale: String,
    var visible: String,
    var statusLabel: String,
)

fun toGlobalSwapPlanView(item: GlobalSwapPlan): GlobalSwapPlanView {
    return GlobalSwapPlanView(
        code = item.code,
        name = truncate(item.name, 120),
        attempts = item.attempts.toString(),
        retries = item.retries.toString(),
        segment = item.segment,
        locale = item.locale,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalSwapPlanViews(items: MutableList<GlobalSwapPlan>): MutableList<GlobalSwapPlanView> {
    val views = mutableListOf<GlobalSwapPlanView>()
    for (item in items) {
        views.add(toGlobalSwapPlanView(item))
    }
    return views
}

fun sumGlobalSwapPlanView(view: GlobalSwapPlanView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedAttempts.toDouble() + parsedRetries.toDouble()
}
