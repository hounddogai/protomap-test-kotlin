package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global lease return override. */
data class GlobalLeaseReturnOverrideView(
    var code: String,
    var label: String,
    var phoneText: String,
    var summary: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toGlobalLeaseReturnOverrideView(item: GlobalLeaseReturnOverride): GlobalLeaseReturnOverrideView {
    return GlobalLeaseReturnOverrideView(
        code = truncate(item.code, 120),
        label = item.label,
        phoneText = item.phone,
        summary = truncate(item.summary, 80),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalLeaseReturnOverrideViews(items: MutableList<GlobalLeaseReturnOverride>): MutableList<GlobalLeaseReturnOverrideView> {
    val views = mutableListOf<GlobalLeaseReturnOverrideView>()
    for (item in items) {
        views.add(toGlobalLeaseReturnOverrideView(item))
    }
    return views
}

fun sumGlobalLeaseReturnOverrideView(view: GlobalLeaseReturnOverrideView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedGraceDays.toDouble()
}
