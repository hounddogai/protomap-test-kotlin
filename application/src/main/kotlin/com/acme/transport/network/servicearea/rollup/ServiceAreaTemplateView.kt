package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area template. */
data class ServiceAreaTemplateView(
    var code: String,
    var title: String,
    var notes: String,
    var cost: String,
    var description: String,
    var expiresAt: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toServiceAreaTemplateView(item: ServiceAreaTemplate): ServiceAreaTemplateView {
    return ServiceAreaTemplateView(
        code = item.code,
        title = item.title,
        notes = truncate(item.notes, 120),
        cost = "%.2f".format(item.cost),
        description = item.description,
        expiresAt = item.expiresAt.toString(),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaTemplateViews(items: MutableList<ServiceAreaTemplate>): MutableList<ServiceAreaTemplateView> {
    val views = mutableListOf<ServiceAreaTemplateView>()
    for (item in items) {
        views.add(toServiceAreaTemplateView(item))
    }
    return views
}

fun sumServiceAreaTemplateView(view: ServiceAreaTemplateView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCost + parsedExpiresAt.toDouble() + parsedCreatedAt.toDouble()
}
