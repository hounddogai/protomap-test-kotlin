package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize

/** Where a adaptive route offset manifest is in its life cycle. */
enum class AdaptiveRouteOffsetManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive route offset manifest as the route offset team keeps it. */
data class AdaptiveRouteOffsetManifest(
    var code: String,
    var label: String,
    var unit: String,
    var source: String,
    var trailerId: String,
    var quantity: Int,
    var title: String,
    var status: AdaptiveRouteOffsetManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRouteOffsetManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRouteOffsetManifestStatus): Boolean {
        when (status) {
            AdaptiveRouteOffsetManifestStatus.DRAFT -> return next == AdaptiveRouteOffsetManifestStatus.ACTIVE
            AdaptiveRouteOffsetManifestStatus.PENDING -> {
                when (next) {
                    AdaptiveRouteOffsetManifestStatus.ACTIVE, AdaptiveRouteOffsetManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetManifestStatus.ACTIVE -> {
                when (next) {
                    AdaptiveRouteOffsetManifestStatus.SUSPENDED, AdaptiveRouteOffsetManifestStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetManifestStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveRouteOffsetManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRouteOffsetManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive route offset manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AdaptiveRouteOffsetManifest {
        return AdaptiveRouteOffsetManifest(
            code = code,
            label = label,
            unit = unit,
            source = source,
            trailerId = trailerId,
            quantity = quantity,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive route offset manifest in status draft. */
fun newAdaptiveRouteOffsetManifest(code: String, label: String): AdaptiveRouteOffsetManifest {
    return AdaptiveRouteOffsetManifest(
        code = code,
        label = label,
        unit = "",
        source = "",
        trailerId = "",
        quantity = 0,
        title = "",
        status = AdaptiveRouteOffsetManifestStatus.DRAFT,
    )
}
