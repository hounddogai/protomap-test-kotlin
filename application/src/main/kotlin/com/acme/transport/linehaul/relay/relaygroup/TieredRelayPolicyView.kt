package com.acme.transport.linehaul.relay.relaygroup

/** How screens and exports show a tiered relay policy. */
data class TieredRelayPolicyView(
    var codeText: String,
    var label: String,
    var lane: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toTieredRelayPolicyView(item: TieredRelayPolicy): TieredRelayPolicyView {
    return TieredRelayPolicyView(
        codeText = item.code,
        label = item.label,
        lane = item.lane,
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRelayPolicyViews(items: MutableList<TieredRelayPolicy>): MutableList<TieredRelayPolicyView> {
    val views = mutableListOf<TieredRelayPolicyView>()
    for (item in items) {
        views.add(toTieredRelayPolicyView(item))
    }
    return views
}

fun sumTieredRelayPolicyView(view: TieredRelayPolicyView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
