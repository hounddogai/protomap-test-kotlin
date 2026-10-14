package com.acme.transport.customs.declaration.batch

/** How screens and exports show a default declaration review. */
data class DefaultDeclarationReviewView(
    var code: String,
    var nameText: String,
    var version: String,
    var rate: String,
    var label: String,
    var updatedAtText: String,
    var endsAtText: String,
    var capacityText: String,
    var limit: String,
    var category: String,
    var statusLabel: String,
)

fun toDefaultDeclarationReviewView(item: DefaultDeclarationReview): DefaultDeclarationReviewView {
    return DefaultDeclarationReviewView(
        code = item.code,
        nameText = item.name,
        version = item.version.toString(),
        rate = "%.2f".format(item.rate),
        label = item.label,
        updatedAtText = item.updatedAt.toString(),
        endsAtText = item.endsAt.toString(),
        capacityText = item.capacity.toString(),
        limit = item.limit.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDeclarationReviewViews(items: MutableList<DefaultDeclarationReview>): MutableList<DefaultDeclarationReviewView> {
    val views = mutableListOf<DefaultDeclarationReviewView>()
    for (item in items) {
        views.add(toDefaultDeclarationReviewView(item))
    }
    return views
}

fun sumDefaultDeclarationReviewView(view: DefaultDeclarationReviewView): Double {
    val parsedVersion = view.version.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedVersion.toDouble() + parsedRate + parsedUpdatedAt.toDouble()
}
