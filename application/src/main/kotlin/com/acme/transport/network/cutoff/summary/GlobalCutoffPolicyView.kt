package com.acme.transport.network.cutoff.summary

/** How screens and exports show a global cutoff policy. */
data class GlobalCutoffPolicyView(
    var code: String,
    var labelText: String,
    var routeCode: String,
    var discount: String,
    var contactEmail: String,
    var rate: String,
    var createdAt: String,
    var attempts: String,
    var startsAt: String,
    var marginText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toGlobalCutoffPolicyView(item: GlobalCutoffPolicy): GlobalCutoffPolicyView {
    return GlobalCutoffPolicyView(
        code = item.code,
        labelText = item.label,
        routeCode = item.routeCode,
        discount = "%.2f".format(item.discount),
        contactEmail = item.contactEmail,
        rate = "%.2f".format(item.rate),
        createdAt = item.createdAt.toString(),
        attempts = item.attempts.toString(),
        startsAt = item.startsAt.toString(),
        marginText = "%.2f".format(item.margin),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCutoffPolicyViews(items: MutableList<GlobalCutoffPolicy>): MutableList<GlobalCutoffPolicyView> {
    val views = mutableListOf<GlobalCutoffPolicyView>()
    for (item in items) {
        views.add(toGlobalCutoffPolicyView(item))
    }
    return views
}

fun sumGlobalCutoffPolicyView(view: GlobalCutoffPolicyView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedDiscount + parsedRate + parsedCreatedAt.toDouble()
}
