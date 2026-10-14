package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dwell exception. */
data class GlobalDwellExceptionView(
    var code: String,
    var title: String,
    var versionText: String,
    var stopCount: String,
    var timezone: String,
    var createdAt: String,
    var channel: String,
    var statusLabel: String,
)

fun toGlobalDwellExceptionView(item: GlobalDwellException): GlobalDwellExceptionView {
    return GlobalDwellExceptionView(
        code = truncate(item.code, 40),
        title = item.title,
        versionText = item.version.toString(),
        stopCount = item.stopCount.toString(),
        timezone = truncate(item.timezone, 120),
        createdAt = item.createdAt.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDwellExceptionViews(items: MutableList<GlobalDwellException>): MutableList<GlobalDwellExceptionView> {
    val views = mutableListOf<GlobalDwellExceptionView>()
    for (item in items) {
        views.add(toGlobalDwellExceptionView(item))
    }
    return views
}

fun sumGlobalDwellExceptionView(view: GlobalDwellExceptionView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedVersion.toDouble() + parsedStopCount.toDouble() + parsedCreatedAt.toDouble()
}
