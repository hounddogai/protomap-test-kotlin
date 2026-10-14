package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial mapping. */
data class AccessorialMappingView(
    var code: String,
    var labelText: String,
    var version: String,
    var batchSizeText: String,
    var country: String,
    var statusLabel: String,
)

fun toAccessorialMappingView(item: AccessorialMapping): AccessorialMappingView {
    return AccessorialMappingView(
        code = item.code,
        labelText = truncate(item.label, 64),
        version = item.version.toString(),
        batchSizeText = item.batchSize.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialMappingViews(items: MutableList<AccessorialMapping>): MutableList<AccessorialMappingView> {
    val views = mutableListOf<AccessorialMappingView>()
    for (item in items) {
        views.add(toAccessorialMappingView(item))
    }
    return views
}

fun sumAccessorialMappingView(view: AccessorialMappingView): Double {
    val parsedVersion = view.version.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedVersion.toDouble() + parsedBatchSize.toDouble()
}
