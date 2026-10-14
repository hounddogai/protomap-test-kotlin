package com.acme.transport.terminal.dockdoor.agreement

/** How screens and exports show a dock door agreement. */
data class DockDoorAgreementView(
    var code: String,
    var nameText: String,
    var revisionText: String,
    var region: String,
    var dockText: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toDockDoorAgreementView(item: DockDoorAgreement): DockDoorAgreementView {
    return DockDoorAgreementView(
        code = item.code,
        nameText = item.name,
        revisionText = item.revision.toString(),
        region = item.region,
        dockText = item.dock,
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorAgreementViews(items: MutableList<DockDoorAgreement>): MutableList<DockDoorAgreementView> {
    val views = mutableListOf<DockDoorAgreementView>()
    for (item in items) {
        views.add(toDockDoorAgreementView(item))
    }
    return views
}

fun sumDockDoorAgreementView(view: DockDoorAgreementView): Double {
    val parsedRevision = view.revisionText.toInt()
    return parsedRevision.toDouble()
}
