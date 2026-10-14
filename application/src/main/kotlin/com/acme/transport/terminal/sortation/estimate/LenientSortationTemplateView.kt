package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient sortation template. */
data class LenientSortationTemplateView(
    var codeText: String,
    var label: String,
    var sequence: String,
    var phone: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toLenientSortationTemplateView(item: LenientSortationTemplate): LenientSortationTemplateView {
    return LenientSortationTemplateView(
        codeText = truncate(item.code, 120),
        label = item.label,
        sequence = item.sequence.toString(),
        phone = item.phone,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientSortationTemplateViews(items: MutableList<LenientSortationTemplate>): MutableList<LenientSortationTemplateView> {
    val views = mutableListOf<LenientSortationTemplateView>()
    for (item in items) {
        views.add(toLenientSortationTemplateView(item))
    }
    return views
}

fun sumLenientSortationTemplateView(view: LenientSortationTemplateView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedSequence.toDouble() + parsedLoadKg
}
