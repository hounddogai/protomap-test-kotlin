package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.normalize

/** Where a adaptive tire manifest is in its life cycle. */
enum class AdaptiveTireManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive tire manifest as the tire team keeps it. */
data class AdaptiveTireManifest(
    var code: String,
    var title: String,
    var description: String,
    var driverId: String,
    var region: String,
    var priority: Int,
    var category: String,
    var status: AdaptiveTireManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTireManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTireManifestStatus): Boolean {
        when (status) {
            AdaptiveTireManifestStatus.OPEN -> return next == AdaptiveTireManifestStatus.IN_REVIEW
            AdaptiveTireManifestStatus.IN_REVIEW -> return next == AdaptiveTireManifestStatus.RESOLVED || next == AdaptiveTireManifestStatus.REOPENED
            AdaptiveTireManifestStatus.RESOLVED -> return next == AdaptiveTireManifestStatus.REOPENED
            AdaptiveTireManifestStatus.REOPENED -> {
                when (next) {
                    AdaptiveTireManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTireManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tire manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): AdaptiveTireManifest {
        return AdaptiveTireManifest(
            code = code,
            title = title,
            description = description,
            driverId = driverId,
            region = region,
            priority = priority,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive tire manifest in status open. */
fun newAdaptiveTireManifest(code: String, title: String): AdaptiveTireManifest {
    return AdaptiveTireManifest(
        code = code,
        title = title,
        description = "",
        driverId = "",
        region = "",
        priority = 0,
        category = "",
        status = AdaptiveTireManifestStatus.OPEN,
    )
}
