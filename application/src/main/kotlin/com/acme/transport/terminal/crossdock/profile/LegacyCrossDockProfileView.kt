package com.acme.transport.terminal.crossdock.profile

/** How screens and exports show a legacy cross dock profile. */
data class LegacyCrossDockProfileView(
    var code: String,
    var label: String,
    var amount: String,
    var contactEmail: String,
    var locale: String,
    var statusLabel: String,
)

fun toLegacyCrossDockProfileView(item: LegacyCrossDockProfile): LegacyCrossDockProfileView {
    return LegacyCrossDockProfileView(
        code = item.code,
        label = item.label,
        amount = "%.2f".format(item.amount),
        contactEmail = item.contactEmail,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCrossDockProfileViews(items: MutableList<LegacyCrossDockProfile>): MutableList<LegacyCrossDockProfileView> {
    val views = mutableListOf<LegacyCrossDockProfileView>()
    for (item in items) {
        views.add(toLegacyCrossDockProfileView(item))
    }
    return views
}

fun sumLegacyCrossDockProfileView(view: LegacyCrossDockProfileView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
