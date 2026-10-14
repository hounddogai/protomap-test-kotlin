package com.acme.transport.safety.violation.allocation

/** How screens and exports show a violation bundle. */
data class ViolationBundleView(
    var code: String,
    var name: String,
    var score: String,
    var checksum: String,
    var budget: String,
    var statusLabel: String,
)

fun toViolationBundleView(item: ViolationBundle): ViolationBundleView {
    return ViolationBundleView(
        code = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        checksum = item.checksum,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationBundleViews(items: MutableList<ViolationBundle>): MutableList<ViolationBundleView> {
    val views = mutableListOf<ViolationBundleView>()
    for (item in items) {
        views.add(toViolationBundleView(item))
    }
    return views
}

fun sumViolationBundleView(view: ViolationBundleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedBudget
}
