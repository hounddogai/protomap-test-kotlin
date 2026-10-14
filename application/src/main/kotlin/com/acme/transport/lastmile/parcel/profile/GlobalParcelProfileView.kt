package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global parcel profile. */
data class GlobalParcelProfileView(
    var code: String,
    var label: String,
    var sequenceText: String,
    var createdAt: String,
    var nameText: String,
    var capacity: String,
    var fuelLiters: String,
    var minimum: String,
    var title: String,
    var budget: String,
    var statusLabel: String,
)

fun toGlobalParcelProfileView(item: GlobalParcelProfile): GlobalParcelProfileView {
    return GlobalParcelProfileView(
        code = truncate(item.code, 40),
        label = item.label,
        sequenceText = item.sequence.toString(),
        createdAt = item.createdAt.toString(),
        nameText = truncate(item.name, 120),
        capacity = item.capacity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        minimum = "%.2f".format(item.minimum),
        title = item.title,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalParcelProfileViews(items: MutableList<GlobalParcelProfile>): MutableList<GlobalParcelProfileView> {
    val views = mutableListOf<GlobalParcelProfileView>()
    for (item in items) {
        views.add(toGlobalParcelProfileView(item))
    }
    return views
}

fun sumGlobalParcelProfileView(view: GlobalParcelProfileView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedSequence.toDouble() + parsedCreatedAt.toDouble() + parsedCapacity.toDouble()
}
