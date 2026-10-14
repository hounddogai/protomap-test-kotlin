package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback appointment bundle. */
data class FallbackAppointmentBundleView(
    var code: String,
    var title: String,
    var ratio: String,
    var createdAt: String,
    var segment: String,
    var limit: String,
    var quantityText: String,
    var notesText: String,
    var approvedText: String,
    var category: String,
    var statusLabel: String,
)

fun toFallbackAppointmentBundleView(item: FallbackAppointmentBundle): FallbackAppointmentBundleView {
    return FallbackAppointmentBundleView(
        code = item.code,
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        createdAt = item.createdAt.toString(),
        segment = truncate(item.segment, 40),
        limit = item.limit.toString(),
        quantityText = item.quantity.toString(),
        notesText = item.notes,
        approvedText = "${item.approved}",
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAppointmentBundleViews(items: MutableList<FallbackAppointmentBundle>): MutableList<FallbackAppointmentBundleView> {
    val views = mutableListOf<FallbackAppointmentBundleView>()
    for (item in items) {
        views.add(toFallbackAppointmentBundleView(item))
    }
    return views
}

fun sumFallbackAppointmentBundleView(view: FallbackAppointmentBundleView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedRatio + parsedCreatedAt.toDouble() + parsedLimit.toDouble()
}
