package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dispute request is in its life cycle. */
enum class LegacyDisputeRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy dispute request, with what the planners need to act on it. */
data class LegacyDisputeRequest(
    var code: String,
    var label: String,
    var odometer: Int,
    var budget: Double,
    var floorValue: Double,
    var status: LegacyDisputeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDisputeRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDisputeRequestStatus): Boolean {
        when (status) {
            LegacyDisputeRequestStatus.OPEN -> return next == LegacyDisputeRequestStatus.REOPENED
            LegacyDisputeRequestStatus.IN_REVIEW -> {
                when (next) {
                    LegacyDisputeRequestStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyDisputeRequestStatus.RESOLVED -> return next == LegacyDisputeRequestStatus.REOPENED || next == LegacyDisputeRequestStatus.ARCHIVED
            LegacyDisputeRequestStatus.REOPENED -> return next == LegacyDisputeRequestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyDisputeRequestStatus) {
        if (!canMoveTo(next)) {
            error("legacy dispute request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((budget - floorValue) * 100) / 100

    fun snapshot(): LegacyDisputeRequest {
        return LegacyDisputeRequest(
            code = code,
            label = label,
            odometer = odometer,
            budget = budget,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy dispute request in status open. */
fun newLegacyDisputeRequest(code: String, label: String): LegacyDisputeRequest {
    return LegacyDisputeRequest(
        code = code,
        label = label,
        odometer = 0,
        budget = 0.0,
        floorValue = 0.0,
        status = LegacyDisputeRequestStatus.OPEN,
    )
}
