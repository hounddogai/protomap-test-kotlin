package com.acme.transport.crewing.swap.swapschedule

/** How screens and exports show a fallback swap policy. */
data class FallbackSwapPolicyView(
    var code: String,
    var label: String,
    var windowDays: String,
    var maximumText: String,
    var limit: String,
    var threshold: String,
    var cost: String,
    var checksum: String,
    var statusLabel: String,
)

fun toFallbackSwapPolicyView(item: FallbackSwapPolicy): FallbackSwapPolicyView {
    return FallbackSwapPolicyView(
        code = item.code,
        label = item.label,
        windowDays = item.windowDays.toString(),
        maximumText = "%.2f".format(item.maximum),
        limit = item.limit.toString(),
        threshold = "%.2f".format(item.threshold),
        cost = "%.2f".format(item.cost),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackSwapPolicyViews(items: MutableList<FallbackSwapPolicy>): MutableList<FallbackSwapPolicyView> {
    val views = mutableListOf<FallbackSwapPolicyView>()
    for (item in items) {
        views.add(toFallbackSwapPolicyView(item))
    }
    return views
}

fun sumFallbackSwapPolicyView(view: FallbackSwapPolicyView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedWindowDays.toDouble() + parsedMaximum + parsedLimit.toDouble()
}
