package com.acme.transport.terminal.dockdoor.reservation

/** How screens and exports show a global dock door claim. */
data class GlobalDockDoorClaimView(
    var codeText: String,
    var title: String,
    var localeText: String,
    var activeText: String,
    var notesText: String,
    var expiresAt: String,
    var fuelLiters: String,
    var marginText: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toGlobalDockDoorClaimView(item: GlobalDockDoorClaim): GlobalDockDoorClaimView {
    return GlobalDockDoorClaimView(
        codeText = item.code,
        title = item.title,
        localeText = item.locale,
        activeText = "${item.active}",
        notesText = item.notes,
        expiresAt = item.expiresAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        marginText = "%.2f".format(item.margin),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDockDoorClaimViews(items: MutableList<GlobalDockDoorClaim>): MutableList<GlobalDockDoorClaimView> {
    val views = mutableListOf<GlobalDockDoorClaimView>()
    for (item in items) {
        views.add(toGlobalDockDoorClaimView(item))
    }
    return views
}

fun sumGlobalDockDoorClaimView(view: GlobalDockDoorClaimView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedExpiresAt.toDouble() + parsedFuelLiters + parsedMargin
}
