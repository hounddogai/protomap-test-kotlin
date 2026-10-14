package com.acme.transport.fleetops.leasereturn.template

/** How screens and exports show a lease return draft. */
data class LeaseReturnDraftView(
    var code: String,
    var name: String,
    var maximumText: String,
    var version: String,
    var updatedAt: String,
    var limit: String,
    var statusLabel: String,
)

fun toLeaseReturnDraftView(item: LeaseReturnDraft): LeaseReturnDraftView {
    return LeaseReturnDraftView(
        code = item.code,
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        version = item.version.toString(),
        updatedAt = item.updatedAt.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnDraftViews(items: MutableList<LeaseReturnDraft>): MutableList<LeaseReturnDraftView> {
    val views = mutableListOf<LeaseReturnDraftView>()
    for (item in items) {
        views.add(toLeaseReturnDraftView(item))
    }
    return views
}

fun sumLeaseReturnDraftView(view: LeaseReturnDraftView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedMaximum + parsedVersion.toDouble() + parsedUpdatedAt.toDouble()
}
