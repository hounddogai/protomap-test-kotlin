package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration group. */
data class DeclarationGroupView(
    var code: String,
    var label: String,
    var score: String,
    var vehicleId: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toDeclarationGroupView(item: DeclarationGroup): DeclarationGroupView {
    return DeclarationGroupView(
        code = truncate(item.code, 120),
        label = item.label,
        score = "%.2f".format(item.score),
        vehicleId = item.vehicleId,
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationGroupViews(items: MutableList<DeclarationGroup>): MutableList<DeclarationGroupView> {
    val views = mutableListOf<DeclarationGroupView>()
    for (item in items) {
        views.add(toDeclarationGroupView(item))
    }
    return views
}

fun sumDeclarationGroupView(view: DeclarationGroupView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedScore + parsedBudget
}
