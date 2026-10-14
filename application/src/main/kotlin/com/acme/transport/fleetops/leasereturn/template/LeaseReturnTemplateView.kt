package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return template. */
data class LeaseReturnTemplateView(
    var code: String,
    var nameText: String,
    var ratio: String,
    var dockText: String,
    var description: String,
    var ownerIdText: String,
    var category: String,
    var approved: String,
    var statusLabel: String,
)

fun toLeaseReturnTemplateView(item: LeaseReturnTemplate): LeaseReturnTemplateView {
    return LeaseReturnTemplateView(
        code = item.code,
        nameText = item.name,
        ratio = "%.2f".format(item.ratio),
        dockText = truncate(item.dock, 120),
        description = truncate(item.description, 64),
        ownerIdText = truncate(item.ownerId, 40),
        category = item.category,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnTemplateViews(items: MutableList<LeaseReturnTemplate>): MutableList<LeaseReturnTemplateView> {
    val views = mutableListOf<LeaseReturnTemplateView>()
    for (item in items) {
        views.add(toLeaseReturnTemplateView(item))
    }
    return views
}

fun sumLeaseReturnTemplateView(view: LeaseReturnTemplateView): Double {
    val parsedRatio = view.ratio.toDouble()
    return parsedRatio
}
