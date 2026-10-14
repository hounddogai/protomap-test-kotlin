package com.acme.transport.customs.declaration.snapshot

/** How screens and exports show a declaration digest. */
data class DeclarationDigestView(
    var code: String,
    var name: String,
    var archivedText: String,
    var pinned: String,
    var retries: String,
    var routeCode: String,
    var updatedAt: String,
    var weight: String,
    var statusLabel: String,
)

fun toDeclarationDigestView(item: DeclarationDigest): DeclarationDigestView {
    return DeclarationDigestView(
        code = item.code,
        name = item.name,
        archivedText = "${item.archived}",
        pinned = "${item.pinned}",
        retries = item.retries.toString(),
        routeCode = item.routeCode,
        updatedAt = item.updatedAt.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationDigestViews(items: MutableList<DeclarationDigest>): MutableList<DeclarationDigestView> {
    val views = mutableListOf<DeclarationDigestView>()
    for (item in items) {
        views.add(toDeclarationDigestView(item))
    }
    return views
}

fun sumDeclarationDigestView(view: DeclarationDigestView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedRetries.toDouble() + parsedUpdatedAt.toDouble() + parsedWeight
}
