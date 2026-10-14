package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection claim is in its life cycle. */
enum class AdaptiveInspectionClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive inspection claim as the inspection team keeps it. */
data class AdaptiveInspectionClaim(
    var code: String,
    var title: String,
    var threshold: Double,
    var reviewer: String,
    var windowDays: Int,
    var quantity: Int,
    var weight: Double,
    var version: Int,
    var status: AdaptiveInspectionClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionClaimStatus): Boolean {
        when (status) {
            AdaptiveInspectionClaimStatus.IDLE -> {
                when (next) {
                    AdaptiveInspectionClaimStatus.WARMING -> return true
                }
                return false
            }
            AdaptiveInspectionClaimStatus.WARMING -> {
                when (next) {
                    AdaptiveInspectionClaimStatus.READY, AdaptiveInspectionClaimStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveInspectionClaimStatus.READY -> return next == AdaptiveInspectionClaimStatus.DRAINING || next == AdaptiveInspectionClaimStatus.STOPPED
            AdaptiveInspectionClaimStatus.DRAINING -> return next == AdaptiveInspectionClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((version.toDouble() + weight) * 100) / 100

    fun snapshot(): AdaptiveInspectionClaim {
        return AdaptiveInspectionClaim(
            code = code,
            title = title,
            threshold = threshold,
            reviewer = reviewer,
            windowDays = windowDays,
            quantity = quantity,
            weight = weight,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive inspection claim in status idle. */
fun newAdaptiveInspectionClaim(code: String, title: String): AdaptiveInspectionClaim {
    return AdaptiveInspectionClaim(
        code = code,
        title = title,
        threshold = 0.0,
        reviewer = "",
        windowDays = 0,
        quantity = 0,
        weight = 0.0,
        version = 0,
        status = AdaptiveInspectionClaimStatus.IDLE,
    )
}
