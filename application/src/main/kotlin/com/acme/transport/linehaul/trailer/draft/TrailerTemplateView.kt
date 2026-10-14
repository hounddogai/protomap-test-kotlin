package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer template. */
data class TrailerTemplateView(
    var code: String,
    var title: String,
    var ratio: String,
    var visible: String,
    var floorValueText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toTrailerTemplateView(item: TrailerTemplate): TrailerTemplateView {
    return TrailerTemplateView(
        code = truncate(item.code, 80),
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        visible = "${item.visible}",
        floorValueText = "%.2f".format(item.floorValue),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerTemplateViews(items: MutableList<TrailerTemplate>): MutableList<TrailerTemplateView> {
    val views = mutableListOf<TrailerTemplateView>()
    for (item in items) {
        views.add(toTrailerTemplateView(item))
    }
    return views
}

fun sumTrailerTemplateView(view: TrailerTemplateView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedRatio + parsedFloorValue
}
