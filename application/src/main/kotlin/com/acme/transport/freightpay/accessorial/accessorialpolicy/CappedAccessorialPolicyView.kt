package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped accessorial policy. */
data class CappedAccessorialPolicyView(
    var code: String,
    var name: String,
    var description: String,
    var dock: String,
    var attempts: String,
    var statusLabel: String,
)

fun toCappedAccessorialPolicyView(item: CappedAccessorialPolicy): CappedAccessorialPolicyView {
    return CappedAccessorialPolicyView(
        code = truncate(item.code, 80),
        name = item.name,
        description = item.description,
        dock = truncate(item.dock, 120),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAccessorialPolicyViews(items: MutableList<CappedAccessorialPolicy>): MutableList<CappedAccessorialPolicyView> {
    val views = mutableListOf<CappedAccessorialPolicyView>()
    for (item in items) {
        views.add(toCappedAccessorialPolicyView(item))
    }
    return views
}

fun sumCappedAccessorialPolicyView(view: CappedAccessorialPolicyView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
