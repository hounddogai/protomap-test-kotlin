package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation request. */
data class ViolationRequestView(
    var codeText: String,
    var nameText: String,
    var discount: String,
    var attempts: String,
    var category: String,
    var statusLabel: String,
)

fun toViolationRequestView(item: ViolationRequest): ViolationRequestView {
    return ViolationRequestView(
        codeText = item.code,
        nameText = item.name,
        discount = "%.2f".format(item.discount),
        attempts = item.attempts.toString(),
        category = truncate(item.category, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationRequestViews(items: MutableList<ViolationRequest>): MutableList<ViolationRequestView> {
    val views = mutableListOf<ViolationRequestView>()
    for (item in items) {
        views.add(toViolationRequestView(item))
    }
    return views
}

fun sumViolationRequestView(view: ViolationRequestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedDiscount + parsedAttempts.toDouble()
}
