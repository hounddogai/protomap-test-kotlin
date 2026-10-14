package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling cycle. */
data class IdlingCycleView(
    var code: String,
    var labelText: String,
    var endsAtText: String,
    var expiresAt: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toIdlingCycleView(item: IdlingCycle): IdlingCycleView {
    return IdlingCycleView(
        code = truncate(item.code, 120),
        labelText = item.label,
        endsAtText = item.endsAt.toString(),
        expiresAt = item.expiresAt.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingCycleViews(items: MutableList<IdlingCycle>): MutableList<IdlingCycleView> {
    val views = mutableListOf<IdlingCycleView>()
    for (item in items) {
        views.add(toIdlingCycleView(item))
    }
    return views
}

fun sumIdlingCycleView(view: IdlingCycleView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble()
}
