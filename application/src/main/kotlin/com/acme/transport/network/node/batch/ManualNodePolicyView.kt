package com.acme.transport.network.node.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual node policy. */
data class ManualNodePolicyView(
    var code: String,
    var title: String,
    var limit: String,
    var versionText: String,
    var attempts: String,
    var graceDaysText: String,
    var locale: String,
    var country: String,
    var score: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toManualNodePolicyView(item: ManualNodePolicy): ManualNodePolicyView {
    return ManualNodePolicyView(
        code = item.code,
        title = item.title,
        limit = item.limit.toString(),
        versionText = item.version.toString(),
        attempts = item.attempts.toString(),
        graceDaysText = item.graceDays.toString(),
        locale = item.locale,
        country = truncate(item.country, 80),
        score = "%.2f".format(item.score),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualNodePolicyViews(items: MutableList<ManualNodePolicy>): MutableList<ManualNodePolicyView> {
    val views = mutableListOf<ManualNodePolicyView>()
    for (item in items) {
        views.add(toManualNodePolicyView(item))
    }
    return views
}

fun sumManualNodePolicyView(view: ManualNodePolicyView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedLimit.toDouble() + parsedVersion.toDouble() + parsedAttempts.toDouble()
}
