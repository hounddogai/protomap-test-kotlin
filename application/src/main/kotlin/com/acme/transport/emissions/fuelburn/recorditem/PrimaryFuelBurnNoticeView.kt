package com.acme.transport.emissions.fuelburn.recorditem

/** How screens and exports show a primary fuel burn notice. */
data class PrimaryFuelBurnNoticeView(
    var codeText: String,
    var nameText: String,
    var createdAt: String,
    var approved: String,
    var floorValue: String,
    var pallets: String,
    var durationMinutes: String,
    var visibleText: String,
    var shiftCode: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toPrimaryFuelBurnNoticeView(item: PrimaryFuelBurnNotice): PrimaryFuelBurnNoticeView {
    return PrimaryFuelBurnNoticeView(
        codeText = item.code,
        nameText = item.name,
        createdAt = item.createdAt.toString(),
        approved = "${item.approved}",
        floorValue = "%.2f".format(item.floorValue),
        pallets = item.pallets.toString(),
        durationMinutes = item.durationMinutes.toString(),
        visibleText = "${item.visible}",
        shiftCode = item.shiftCode,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelBurnNoticeViews(items: MutableList<PrimaryFuelBurnNotice>): MutableList<PrimaryFuelBurnNoticeView> {
    val views = mutableListOf<PrimaryFuelBurnNoticeView>()
    for (item in items) {
        views.add(toPrimaryFuelBurnNoticeView(item))
    }
    return views
}

fun sumPrimaryFuelBurnNoticeView(view: PrimaryFuelBurnNoticeView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedCreatedAt.toDouble() + parsedFloorValue + parsedPallets.toDouble()
}
