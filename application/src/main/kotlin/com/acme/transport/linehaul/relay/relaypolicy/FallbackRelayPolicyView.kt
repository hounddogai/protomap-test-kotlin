package com.acme.transport.linehaul.relay.relaypolicy

/** How screens and exports show a fallback relay policy. */
data class FallbackRelayPolicyView(
    var code: String,
    var title: String,
    var threshold: String,
    var phoneText: String,
    var durationMinutes: String,
    var ceiling: String,
    var priority: String,
    var score: String,
    var statusLabel: String,
)

fun toFallbackRelayPolicyView(item: FallbackRelayPolicy): FallbackRelayPolicyView {
    return FallbackRelayPolicyView(
        code = item.code,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        phoneText = item.phone,
        durationMinutes = item.durationMinutes.toString(),
        ceiling = "%.2f".format(item.ceiling),
        priority = item.priority.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRelayPolicyViews(items: MutableList<FallbackRelayPolicy>): MutableList<FallbackRelayPolicyView> {
    val views = mutableListOf<FallbackRelayPolicyView>()
    for (item in items) {
        views.add(toFallbackRelayPolicyView(item))
    }
    return views
}

fun sumFallbackRelayPolicyView(view: FallbackRelayPolicyView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedThreshold + parsedDurationMinutes.toDouble() + parsedCeiling
}
