package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped coaching plan. */
data class CappedCoachingPlanView(
    var code: String,
    var title: String,
    var createdAtText: String,
    var batchSize: String,
    var routeCode: String,
    var phone: String,
    var country: String,
    var attempts: String,
    var depot: String,
    var retries: String,
    var statusLabel: String,
)

fun toCappedCoachingPlanView(item: CappedCoachingPlan): CappedCoachingPlanView {
    return CappedCoachingPlanView(
        code = item.code,
        title = item.title,
        createdAtText = item.createdAt.toString(),
        batchSize = item.batchSize.toString(),
        routeCode = item.routeCode,
        phone = truncate(item.phone, 80),
        country = item.country,
        attempts = item.attempts.toString(),
        depot = item.depot,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCoachingPlanViews(items: MutableList<CappedCoachingPlan>): MutableList<CappedCoachingPlanView> {
    val views = mutableListOf<CappedCoachingPlanView>()
    for (item in items) {
        views.add(toCappedCoachingPlanView(item))
    }
    return views
}

fun sumCappedCoachingPlanView(view: CappedCoachingPlanView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedCreatedAt.toDouble() + parsedBatchSize.toDouble() + parsedAttempts.toDouble()
}
