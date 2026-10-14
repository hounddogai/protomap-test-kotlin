package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dock door agreement. */
data class RollingDockDoorAgreementView(
    var code: String,
    var title: String,
    var visible: String,
    var phone: String,
    var contactEmailText: String,
    var depotText: String,
    var externalRefText: String,
    var floorValueText: String,
    var labelText: String,
    var statusLabel: String,
)

fun toRollingDockDoorAgreementView(item: RollingDockDoorAgreement): RollingDockDoorAgreementView {
    return RollingDockDoorAgreementView(
        code = truncate(item.code, 64),
        title = item.title,
        visible = "${item.visible}",
        phone = truncate(item.phone, 40),
        contactEmailText = item.contactEmail,
        depotText = item.depot,
        externalRefText = item.externalRef,
        floorValueText = "%.2f".format(item.floorValue),
        labelText = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDockDoorAgreementViews(items: MutableList<RollingDockDoorAgreement>): MutableList<RollingDockDoorAgreementView> {
    val views = mutableListOf<RollingDockDoorAgreementView>()
    for (item in items) {
        views.add(toRollingDockDoorAgreementView(item))
    }
    return views
}

fun sumRollingDockDoorAgreementView(view: RollingDockDoorAgreementView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedFloorValue
}
