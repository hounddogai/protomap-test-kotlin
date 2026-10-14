package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary declaration draft. */
data class PrimaryDeclarationDraftView(
    var code: String,
    var label: String,
    var externalRef: String,
    var name: String,
    var etaMinutes: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toPrimaryDeclarationDraftView(item: PrimaryDeclarationDraft): PrimaryDeclarationDraftView {
    return PrimaryDeclarationDraftView(
        code = truncate(item.code, 64),
        label = item.label,
        externalRef = item.externalRef,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        reviewer = truncate(item.reviewer, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDeclarationDraftViews(items: MutableList<PrimaryDeclarationDraft>): MutableList<PrimaryDeclarationDraftView> {
    val views = mutableListOf<PrimaryDeclarationDraftView>()
    for (item in items) {
        views.add(toPrimaryDeclarationDraftView(item))
    }
    return views
}

fun sumPrimaryDeclarationDraftView(view: PrimaryDeclarationDraftView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
