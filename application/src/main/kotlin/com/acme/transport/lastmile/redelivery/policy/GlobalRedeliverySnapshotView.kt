package com.acme.transport.lastmile.redelivery.policy

/** How screens and exports show a global redelivery snapshot. */
data class GlobalRedeliverySnapshotView(
    var code: String,
    var nameText: String,
    var active: String,
    var createdAt: String,
    var region: String,
    var expiresAt: String,
    var category: String,
    var priority: String,
    var statusLabel: String,
)

fun toGlobalRedeliverySnapshotView(item: GlobalRedeliverySnapshot): GlobalRedeliverySnapshotView {
    return GlobalRedeliverySnapshotView(
        code = item.code,
        nameText = item.name,
        active = "${item.active}",
        createdAt = item.createdAt.toString(),
        region = item.region,
        expiresAt = item.expiresAt.toString(),
        category = item.category,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRedeliverySnapshotViews(items: MutableList<GlobalRedeliverySnapshot>): MutableList<GlobalRedeliverySnapshotView> {
    val views = mutableListOf<GlobalRedeliverySnapshotView>()
    for (item in items) {
        views.add(toGlobalRedeliverySnapshotView(item))
    }
    return views
}

fun sumGlobalRedeliverySnapshotView(view: GlobalRedeliverySnapshotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCreatedAt.toDouble() + parsedExpiresAt.toDouble() + parsedPriority.toDouble()
}
