package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global sortation bundle. */
data class GlobalSortationBundleView(
    var codeText: String,
    var title: String,
    var archived: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toGlobalSortationBundleView(item: GlobalSortationBundle): GlobalSortationBundleView {
    return GlobalSortationBundleView(
        codeText = truncate(item.code, 120),
        title = item.title,
        archived = "${item.archived}",
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalSortationBundleViews(items: MutableList<GlobalSortationBundle>): MutableList<GlobalSortationBundleView> {
    val views = mutableListOf<GlobalSortationBundleView>()
    for (item in items) {
        views.add(toGlobalSortationBundleView(item))
    }
    return views
}

fun sumGlobalSortationBundleView(view: GlobalSortationBundleView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    return parsedMinimum
}
