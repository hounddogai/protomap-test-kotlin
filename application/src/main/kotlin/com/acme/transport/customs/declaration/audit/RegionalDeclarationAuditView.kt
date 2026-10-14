package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional declaration audit. */
data class RegionalDeclarationAuditView(
    var codeText: String,
    var label: String,
    var checksumText: String,
    var routeCodeText: String,
    var expiresAt: String,
    var minimum: String,
    var archived: String,
    var title: String,
    var contactEmail: String,
    var score: String,
    var statusLabel: String,
)

fun toRegionalDeclarationAuditView(item: RegionalDeclarationAudit): RegionalDeclarationAuditView {
    return RegionalDeclarationAuditView(
        codeText = item.code,
        label = item.label,
        checksumText = item.checksum,
        routeCodeText = truncate(item.routeCode, 40),
        expiresAt = item.expiresAt.toString(),
        minimum = "%.2f".format(item.minimum),
        archived = "${item.archived}",
        title = item.title,
        contactEmail = truncate(item.contactEmail, 120),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDeclarationAuditViews(items: MutableList<RegionalDeclarationAudit>): MutableList<RegionalDeclarationAuditView> {
    val views = mutableListOf<RegionalDeclarationAuditView>()
    for (item in items) {
        views.add(toRegionalDeclarationAuditView(item))
    }
    return views
}

fun sumRegionalDeclarationAuditView(view: RegionalDeclarationAuditView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedExpiresAt.toDouble() + parsedMinimum + parsedScore
}
