package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss plan. */
data class NearMissPlanView(
    var codeText: String,
    var label: String,
    var title: String,
    var dockText: String,
    var rate: String,
    var ceilingText: String,
    var score: String,
    var channel: String,
    var country: String,
    var statusLabel: String,
)

fun toNearMissPlanView(item: NearMissPlan): NearMissPlanView {
    return NearMissPlanView(
        codeText = item.code,
        label = item.label,
        title = item.title,
        dockText = item.dock,
        rate = "%.2f".format(item.rate),
        ceilingText = "%.2f".format(item.ceiling),
        score = "%.2f".format(item.score),
        channel = truncate(item.channel, 120),
        country = truncate(item.country, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissPlanViews(items: MutableList<NearMissPlan>): MutableList<NearMissPlanView> {
    val views = mutableListOf<NearMissPlanView>()
    for (item in items) {
        views.add(toNearMissPlanView(item))
    }
    return views
}

fun sumNearMissPlanView(view: NearMissPlanView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRate + parsedCeiling + parsedScore
}
