package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global cutoff profile. */
data class GlobalCutoffProfileView(
    var codeText: String,
    var title: String,
    var ownerIdText: String,
    var unit: String,
    var summary: String,
    var country: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toGlobalCutoffProfileView(item: GlobalCutoffProfile): GlobalCutoffProfileView {
    return GlobalCutoffProfileView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 64),
        ownerIdText = item.ownerId,
        unit = item.unit,
        summary = item.summary,
        country = item.country,
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCutoffProfileViews(items: MutableList<GlobalCutoffProfile>): MutableList<GlobalCutoffProfileView> {
    val views = mutableListOf<GlobalCutoffProfileView>()
    for (item in items) {
        views.add(toGlobalCutoffProfileView(item))
    }
    return views
}

fun sumGlobalCutoffProfileView(view: GlobalCutoffProfileView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCeiling
}
