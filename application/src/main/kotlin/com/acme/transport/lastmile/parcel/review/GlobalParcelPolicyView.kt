package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global parcel policy. */
data class GlobalParcelPolicyView(
    var codeText: String,
    var labelText: String,
    var expiresAtText: String,
    var lane: String,
    var marginText: String,
    var createdAt: String,
    var priority: String,
    var statusLabel: String,
)

fun toGlobalParcelPolicyView(item: GlobalParcelPolicy): GlobalParcelPolicyView {
    return GlobalParcelPolicyView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 80),
        expiresAtText = item.expiresAt.toString(),
        lane = item.lane,
        marginText = "%.2f".format(item.margin),
        createdAt = item.createdAt.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalParcelPolicyViews(items: MutableList<GlobalParcelPolicy>): MutableList<GlobalParcelPolicyView> {
    val views = mutableListOf<GlobalParcelPolicyView>()
    for (item in items) {
        views.add(toGlobalParcelPolicyView(item))
    }
    return views
}

fun sumGlobalParcelPolicyView(view: GlobalParcelPolicyView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedExpiresAt.toDouble() + parsedMargin + parsedCreatedAt.toDouble()
}
