package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global qualification policy. */
data class GlobalQualificationPolicyView(
    var codeText: String,
    var title: String,
    var trailerId: String,
    var attempts: String,
    var updatedAt: String,
    var minimum: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toGlobalQualificationPolicyView(item: GlobalQualificationPolicy): GlobalQualificationPolicyView {
    return GlobalQualificationPolicyView(
        codeText = item.code,
        title = truncate(item.title, 64),
        trailerId = item.trailerId,
        attempts = item.attempts.toString(),
        updatedAt = item.updatedAt.toString(),
        minimum = "%.2f".format(item.minimum),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalQualificationPolicyViews(items: MutableList<GlobalQualificationPolicy>): MutableList<GlobalQualificationPolicyView> {
    val views = mutableListOf<GlobalQualificationPolicyView>()
    for (item in items) {
        views.add(toGlobalQualificationPolicyView(item))
    }
    return views
}

fun sumGlobalQualificationPolicyView(view: GlobalQualificationPolicyView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAttempts.toDouble() + parsedUpdatedAt.toDouble() + parsedMinimum
}
