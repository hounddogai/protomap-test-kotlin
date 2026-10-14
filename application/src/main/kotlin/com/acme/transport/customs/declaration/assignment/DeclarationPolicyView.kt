package com.acme.transport.customs.declaration.assignment

/** How screens and exports show a declaration policy. */
data class DeclarationPolicyView(
    var code: String,
    var label: String,
    var version: String,
    var vehicleId: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toDeclarationPolicyView(item: DeclarationPolicy): DeclarationPolicyView {
    return DeclarationPolicyView(
        code = item.code,
        label = item.label,
        version = item.version.toString(),
        vehicleId = item.vehicleId,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationPolicyViews(items: MutableList<DeclarationPolicy>): MutableList<DeclarationPolicyView> {
    val views = mutableListOf<DeclarationPolicyView>()
    for (item in items) {
        views.add(toDeclarationPolicyView(item))
    }
    return views
}

fun sumDeclarationPolicyView(view: DeclarationPolicyView): Double {
    val parsedVersion = view.version.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedVersion.toDouble() + parsedEndsAt.toDouble()
}
