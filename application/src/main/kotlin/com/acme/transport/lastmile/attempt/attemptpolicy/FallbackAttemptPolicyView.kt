package com.acme.transport.lastmile.attempt.attemptpolicy

/** How screens and exports show a fallback attempt policy. */
data class FallbackAttemptPolicyView(
    var code: String,
    var nameText: String,
    var checksumText: String,
    var retries: String,
    var etaMinutes: String,
    var locale: String,
    var statusLabel: String,
)

fun toFallbackAttemptPolicyView(item: FallbackAttemptPolicy): FallbackAttemptPolicyView {
    return FallbackAttemptPolicyView(
        code = item.code,
        nameText = item.name,
        checksumText = item.checksum,
        retries = item.retries.toString(),
        etaMinutes = item.etaMinutes.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAttemptPolicyViews(items: MutableList<FallbackAttemptPolicy>): MutableList<FallbackAttemptPolicyView> {
    val views = mutableListOf<FallbackAttemptPolicyView>()
    for (item in items) {
        views.add(toFallbackAttemptPolicyView(item))
    }
    return views
}

fun sumFallbackAttemptPolicyView(view: FallbackAttemptPolicyView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRetries.toDouble() + parsedEtaMinutes.toDouble()
}
