package com.acme.transport.terminal.dwell.dwelltemplate

/** How screens and exports show a dwell exception. */
data class DwellExceptionView(
    var code: String,
    var name: String,
    var externalRef: String,
    var startsAt: String,
    var limit: String,
    var ceilingText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toDwellExceptionView(item: DwellException): DwellExceptionView {
    return DwellExceptionView(
        code = item.code,
        name = item.name,
        externalRef = item.externalRef,
        startsAt = item.startsAt.toString(),
        limit = item.limit.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellExceptionViews(items: MutableList<DwellException>): MutableList<DwellExceptionView> {
    val views = mutableListOf<DwellExceptionView>()
    for (item in items) {
        views.add(toDwellExceptionView(item))
    }
    return views
}

fun sumDwellExceptionView(view: DwellExceptionView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedStartsAt.toDouble() + parsedLimit.toDouble() + parsedCeiling
}
