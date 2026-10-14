package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection policy is in its life cycle. */
enum class AdaptiveInspectionPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a adaptive inspection policy and its current status. */
data class AdaptiveInspectionPolicy(
    var code: String,
    var name: String,
    var margin: Double,
    var odometer: Int,
    var lane: String,
    var checksum: String,
    var status: AdaptiveInspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionPolicyStatus): Boolean {
        when (status) {
            AdaptiveInspectionPolicyStatus.PROPOSED -> return next == AdaptiveInspectionPolicyStatus.REJECTED
            AdaptiveInspectionPolicyStatus.APPROVED -> return next == AdaptiveInspectionPolicyStatus.REJECTED
            AdaptiveInspectionPolicyStatus.REJECTED -> return next == AdaptiveInspectionPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive inspection policy"} $code"

    fun weightedLoad(): Double = round((margin - odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveInspectionPolicy {
        return AdaptiveInspectionPolicy(
            code = code,
            name = name,
            margin = margin,
            odometer = odometer,
            lane = lane,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive inspection policy in status proposed. */
fun newAdaptiveInspectionPolicy(code: String, name: String): AdaptiveInspectionPolicy {
    return AdaptiveInspectionPolicy(
        code = code,
        name = name,
        margin = 0.0,
        odometer = 0,
        lane = "",
        checksum = "",
        status = AdaptiveInspectionPolicyStatus.PROPOSED,
    )
}
