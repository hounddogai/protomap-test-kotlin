package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.text.normalize

/** Where a legacy rest break agreement is in its life cycle. */
enum class LegacyRestBreakAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy rest break agreement and its current status. */
data class LegacyRestBreakAgreement(
    var code: String,
    var label: String,
    var segment: String,
    var lane: String,
    var vehicleId: String,
    var fuelLiters: Double,
    var reviewer: String,
    var status: LegacyRestBreakAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRestBreakAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRestBreakAgreementStatus): Boolean {
        when (status) {
            LegacyRestBreakAgreementStatus.PROPOSED -> {
                when (next) {
                    LegacyRestBreakAgreementStatus.REJECTED -> return true
                }
                return false
            }
            LegacyRestBreakAgreementStatus.APPROVED -> return next == LegacyRestBreakAgreementStatus.WITHDRAWN
            LegacyRestBreakAgreementStatus.REJECTED -> return next == LegacyRestBreakAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyRestBreakAgreementStatus) {
        if (!canMoveTo(next)) {
            error("legacy rest break agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy rest break agreement"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): LegacyRestBreakAgreement {
        return LegacyRestBreakAgreement(
            code = code,
            label = label,
            segment = segment,
            lane = lane,
            vehicleId = vehicleId,
            fuelLiters = fuelLiters,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy rest break agreement in status proposed. */
fun newLegacyRestBreakAgreement(code: String, label: String): LegacyRestBreakAgreement {
    return LegacyRestBreakAgreement(
        code = code,
        label = label,
        segment = "",
        lane = "",
        vehicleId = "",
        fuelLiters = 0.0,
        reviewer = "",
        status = LegacyRestBreakAgreementStatus.PROPOSED,
    )
}
