package com.acme.transport.customs.declaration.exception

/** How screens and exports show a declaration exception. */
data class DeclarationExceptionView(
    var codeText: String,
    var nameText: String,
    var currencyText: String,
    var revision: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toDeclarationExceptionView(item: DeclarationException): DeclarationExceptionView {
    return DeclarationExceptionView(
        codeText = item.code,
        nameText = item.name,
        currencyText = item.currency,
        revision = item.revision.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationExceptionViews(items: MutableList<DeclarationException>): MutableList<DeclarationExceptionView> {
    val views = mutableListOf<DeclarationExceptionView>()
    for (item in items) {
        views.add(toDeclarationExceptionView(item))
    }
    return views
}

fun sumDeclarationExceptionView(view: DeclarationExceptionView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedRevision.toDouble() + parsedRatio
}
