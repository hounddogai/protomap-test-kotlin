package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict violation policy. */
data class StrictViolationPolicyView(
    var codeText: String,
    var name: String,
    var windowDays: String,
    var ownerId: String,
    var endsAt: String,
    var notesText: String,
    var createdAt: String,
    var externalRef: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toStrictViolationPolicyView(item: StrictViolationPolicy): StrictViolationPolicyView {
    return StrictViolationPolicyView(
        codeText = item.code,
        name = truncate(item.name, 40),
        windowDays = item.windowDays.toString(),
        ownerId = item.ownerId,
        endsAt = item.endsAt.toString(),
        notesText = truncate(item.notes, 40),
        createdAt = item.createdAt.toString(),
        externalRef = item.externalRef,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictViolationPolicyViews(items: MutableList<StrictViolationPolicy>): MutableList<StrictViolationPolicyView> {
    val views = mutableListOf<StrictViolationPolicyView>()
    for (item in items) {
        views.add(toStrictViolationPolicyView(item))
    }
    return views
}

fun sumStrictViolationPolicyView(view: StrictViolationPolicyView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedWindowDays.toDouble() + parsedEndsAt.toDouble() + parsedCreatedAt.toDouble()
}
