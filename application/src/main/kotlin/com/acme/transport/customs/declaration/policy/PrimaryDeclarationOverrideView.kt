package com.acme.transport.customs.declaration.policy

/** How screens and exports show a primary declaration override. */
data class PrimaryDeclarationOverrideView(
    var codeText: String,
    var title: String,
    var segmentText: String,
    var sourceText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toPrimaryDeclarationOverrideView(item: PrimaryDeclarationOverride): PrimaryDeclarationOverrideView {
    return PrimaryDeclarationOverrideView(
        codeText = item.code,
        title = item.title,
        segmentText = item.segment,
        sourceText = item.source,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDeclarationOverrideViews(items: MutableList<PrimaryDeclarationOverride>): MutableList<PrimaryDeclarationOverrideView> {
    val views = mutableListOf<PrimaryDeclarationOverrideView>()
    for (item in items) {
        views.add(toPrimaryDeclarationOverrideView(item))
    }
    return views
}

fun sumPrimaryDeclarationOverrideView(view: PrimaryDeclarationOverrideView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
