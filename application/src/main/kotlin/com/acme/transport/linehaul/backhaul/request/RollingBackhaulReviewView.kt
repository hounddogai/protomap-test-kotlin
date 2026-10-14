package com.acme.transport.linehaul.backhaul.request

/** How screens and exports show a rolling backhaul review. */
data class RollingBackhaulReviewView(
    var codeText: String,
    var nameText: String,
    var createdAtText: String,
    var category: String,
    var versionText: String,
    var statusLabel: String,
)

fun toRollingBackhaulReviewView(item: RollingBackhaulReview): RollingBackhaulReviewView {
    return RollingBackhaulReviewView(
        codeText = item.code,
        nameText = item.name,
        createdAtText = item.createdAt.toString(),
        category = item.category,
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingBackhaulReviewViews(items: MutableList<RollingBackhaulReview>): MutableList<RollingBackhaulReviewView> {
    val views = mutableListOf<RollingBackhaulReviewView>()
    for (item in items) {
        views.add(toRollingBackhaulReviewView(item))
    }
    return views
}

fun sumRollingBackhaulReviewView(view: RollingBackhaulReviewView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedCreatedAt.toDouble() + parsedVersion.toDouble()
}
