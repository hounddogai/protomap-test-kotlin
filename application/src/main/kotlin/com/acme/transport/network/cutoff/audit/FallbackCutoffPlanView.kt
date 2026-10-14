package com.acme.transport.network.cutoff.audit

/** How screens and exports show a fallback cutoff plan. */
data class FallbackCutoffPlanView(
    var codeText: String,
    var name: String,
    var routeCode: String,
    var notes: String,
    var rateText: String,
    var statusLabel: String,
)

fun toFallbackCutoffPlanView(item: FallbackCutoffPlan): FallbackCutoffPlanView {
    return FallbackCutoffPlanView(
        codeText = item.code,
        name = item.name,
        routeCode = item.routeCode,
        notes = item.notes,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCutoffPlanViews(items: MutableList<FallbackCutoffPlan>): MutableList<FallbackCutoffPlanView> {
    val views = mutableListOf<FallbackCutoffPlanView>()
    for (item in items) {
        views.add(toFallbackCutoffPlanView(item))
    }
    return views
}

fun sumFallbackCutoffPlanView(view: FallbackCutoffPlanView): Double {
    val parsedRate = view.rateText.toDouble()
    return parsedRate
}
