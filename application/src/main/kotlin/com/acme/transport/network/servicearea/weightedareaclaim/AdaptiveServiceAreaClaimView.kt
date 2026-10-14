package com.acme.transport.network.servicearea.weightedareaclaim

/** How screens and exports show a adaptive service area claim. */
data class AdaptiveServiceAreaClaimView(
    var code: String,
    var title: String,
    var phone: String,
    var reviewerText: String,
    var attempts: String,
    var name: String,
    var statusLabel: String,
)

fun toAdaptiveServiceAreaClaimView(item: AdaptiveServiceAreaClaim): AdaptiveServiceAreaClaimView {
    return AdaptiveServiceAreaClaimView(
        code = item.code,
        title = item.title,
        phone = item.phone,
        reviewerText = item.reviewer,
        attempts = item.attempts.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveServiceAreaClaimViews(items: MutableList<AdaptiveServiceAreaClaim>): MutableList<AdaptiveServiceAreaClaimView> {
    val views = mutableListOf<AdaptiveServiceAreaClaimView>()
    for (item in items) {
        views.add(toAdaptiveServiceAreaClaimView(item))
    }
    return views
}

fun sumAdaptiveServiceAreaClaimView(view: AdaptiveServiceAreaClaimView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
