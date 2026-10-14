package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration rule. */
data class DeclarationRuleView(
    var code: String,
    var name: String,
    var attempts: String,
    var externalRef: String,
    var notes: String,
    var statusLabel: String,
)

fun toDeclarationRuleView(item: DeclarationRule): DeclarationRuleView {
    return DeclarationRuleView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 40),
        attempts = item.attempts.toString(),
        externalRef = item.externalRef,
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationRuleViews(items: MutableList<DeclarationRule>): MutableList<DeclarationRuleView> {
    val views = mutableListOf<DeclarationRuleView>()
    for (item in items) {
        views.add(toDeclarationRuleView(item))
    }
    return views
}

fun sumDeclarationRuleView(view: DeclarationRuleView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
