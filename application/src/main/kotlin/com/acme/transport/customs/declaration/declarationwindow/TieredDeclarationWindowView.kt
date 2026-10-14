package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered declaration window. */
data class TieredDeclarationWindowView(
    var code: String,
    var titleText: String,
    var attempts: String,
    var notes: String,
    var updatedAt: String,
    var priority: String,
    var description: String,
    var statusLabel: String,
)

fun toTieredDeclarationWindowView(item: TieredDeclarationWindow): TieredDeclarationWindowView {
    return TieredDeclarationWindowView(
        code = truncate(item.code, 80),
        titleText = item.title,
        attempts = item.attempts.toString(),
        notes = item.notes,
        updatedAt = item.updatedAt.toString(),
        priority = item.priority.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDeclarationWindowViews(items: MutableList<TieredDeclarationWindow>): MutableList<TieredDeclarationWindowView> {
    val views = mutableListOf<TieredDeclarationWindowView>()
    for (item in items) {
        views.add(toTieredDeclarationWindowView(item))
    }
    return views
}

fun sumTieredDeclarationWindowView(view: TieredDeclarationWindowView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedAttempts.toDouble() + parsedUpdatedAt.toDouble() + parsedPriority.toDouble()
}
