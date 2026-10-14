package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan bundle is in its life cycle. */
enum class AdaptiveLanePlanBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive lane plan bundle as the lane plan team keeps it. */
data class AdaptiveLanePlanBundle(
    var code: String,
    var label: String,
    var stopCount: Int,
    var score: Double,
    var floorValue: Double,
    var driverId: String,
    var externalRef: String,
    var pallets: Int,
    var pinned: Boolean,
    var notes: String,
    var status: AdaptiveLanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanBundleStatus): Boolean {
        when (status) {
            AdaptiveLanePlanBundleStatus.PROPOSED -> {
                when (next) {
                    AdaptiveLanePlanBundleStatus.APPROVED, AdaptiveLanePlanBundleStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveLanePlanBundleStatus.APPROVED -> {
                when (next) {
                    AdaptiveLanePlanBundleStatus.REJECTED, AdaptiveLanePlanBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveLanePlanBundleStatus.REJECTED -> {
                when (next) {
                    AdaptiveLanePlanBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((stopCount.toDouble() - floorValue) * 100) / 100

    fun snapshot(): AdaptiveLanePlanBundle {
        return AdaptiveLanePlanBundle(
            code = code,
            label = label,
            stopCount = stopCount,
            score = score,
            floorValue = floorValue,
            driverId = driverId,
            externalRef = externalRef,
            pallets = pallets,
            pinned = pinned,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive lane plan bundle in status proposed. */
fun newAdaptiveLanePlanBundle(code: String, label: String): AdaptiveLanePlanBundle {
    return AdaptiveLanePlanBundle(
        code = code,
        label = label,
        stopCount = 0,
        score = 0.0,
        floorValue = 0.0,
        driverId = "",
        externalRef = "",
        pallets = 0,
        pinned = false,
        notes = "",
        status = AdaptiveLanePlanBundleStatus.PROPOSED,
    )
}
