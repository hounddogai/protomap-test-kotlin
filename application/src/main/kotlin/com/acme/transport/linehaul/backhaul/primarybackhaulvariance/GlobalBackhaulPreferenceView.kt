package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global backhaul preference. */
data class GlobalBackhaulPreferenceView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var endsAt: String,
    var priority: String,
    var visible: String,
    var externalRef: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toGlobalBackhaulPreferenceView(item: GlobalBackhaulPreference): GlobalBackhaulPreferenceView {
    return GlobalBackhaulPreferenceView(
        code = truncate(item.code, 40),
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        endsAt = item.endsAt.toString(),
        priority = item.priority.toString(),
        visible = "${item.visible}",
        externalRef = truncate(item.externalRef, 40),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBackhaulPreferenceViews(items: MutableList<GlobalBackhaulPreference>): MutableList<GlobalBackhaulPreferenceView> {
    val views = mutableListOf<GlobalBackhaulPreferenceView>()
    for (item in items) {
        views.add(toGlobalBackhaulPreferenceView(item))
    }
    return views
}

fun sumGlobalBackhaulPreferenceView(view: GlobalBackhaulPreferenceView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedExpiresAt.toDouble() + parsedEndsAt.toDouble() + parsedPriority.toDouble()
}
