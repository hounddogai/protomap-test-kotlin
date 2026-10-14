package com.acme.transport.network.cutoff.slot

/** How screens and exports show a cutoff digest. */
data class CutoffDigestView(
    var code: String,
    var name: String,
    var summary: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toCutoffDigestView(item: CutoffDigest): CutoffDigestView {
    return CutoffDigestView(
        code = item.code,
        name = item.name,
        summary = item.summary,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffDigestViews(items: MutableList<CutoffDigest>): MutableList<CutoffDigestView> {
    val views = mutableListOf<CutoffDigestView>()
    for (item in items) {
        views.add(toCutoffDigestView(item))
    }
    return views
}

fun sumCutoffDigestView(view: CutoffDigestView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedUpdatedAt.toDouble()
}
