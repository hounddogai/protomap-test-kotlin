package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive clearance estimate is in its life cycle. */
enum class AdaptiveClearanceEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive clearance estimate. Its code is unique within a tenant. */
data class AdaptiveClearanceEstimate(
    var code: String,
    var label: String,
    var cost: Double,
    var shiftCode: String,
    var pallets: Int,
    var lane: String,
    var country: String,
    var reviewer: String,
    var distanceKm: Double,
    var margin: Double,
    var status: AdaptiveClearanceEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveClearanceEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveClearanceEstimateStatus): Boolean {
        when (status) {
            AdaptiveClearanceEstimateStatus.NEW -> {
                when (next) {
                    AdaptiveClearanceEstimateStatus.QUEUED, AdaptiveClearanceEstimateStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveClearanceEstimateStatus.QUEUED -> return next == AdaptiveClearanceEstimateStatus.RUNNING || next == AdaptiveClearanceEstimateStatus.DONE
            AdaptiveClearanceEstimateStatus.RUNNING -> return next == AdaptiveClearanceEstimateStatus.DONE
            AdaptiveClearanceEstimateStatus.DONE -> {
                when (next) {
                    AdaptiveClearanceEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveClearanceEstimateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive clearance estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedTotal(): Double = round(cost * distanceKm * 100) / 100

    fun snapshot(): AdaptiveClearanceEstimate {
        return AdaptiveClearanceEstimate(
            code = code,
            label = label,
            cost = cost,
            shiftCode = shiftCode,
            pallets = pallets,
            lane = lane,
            country = country,
            reviewer = reviewer,
            distanceKm = distanceKm,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive clearance estimate in status new. */
fun newAdaptiveClearanceEstimate(code: String, label: String): AdaptiveClearanceEstimate {
    return AdaptiveClearanceEstimate(
        code = code,
        label = label,
        cost = 0.0,
        shiftCode = "",
        pallets = 0,
        lane = "",
        country = "",
        reviewer = "",
        distanceKm = 0.0,
        margin = 0.0,
        status = AdaptiveClearanceEstimateStatus.NEW,
    )
}
