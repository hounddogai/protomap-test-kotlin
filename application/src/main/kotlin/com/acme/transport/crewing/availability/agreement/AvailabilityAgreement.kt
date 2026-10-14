package com.acme.transport.crewing.availability.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability agreement records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability agreement is in its life cycle. */
enum class AvailabilityAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A availability agreement. Its code is unique within a tenant. */
data class AvailabilityAgreement(
    var code: String,
    var label: String,
    var lane: String,
    var odometer: Int,
    var ratio: Double,
    var dock: String,
    var attempts: Int,
    var region: String,
    var routeCode: String,
    var status: AvailabilityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityAgreementStatus): Boolean {
        when (status) {
            AvailabilityAgreementStatus.PROPOSED -> {
                when (next) {
                    AvailabilityAgreementStatus.REJECTED, AvailabilityAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            AvailabilityAgreementStatus.APPROVED -> return next == AvailabilityAgreementStatus.REJECTED || next == AvailabilityAgreementStatus.WITHDRAWN
            AvailabilityAgreementStatus.REJECTED -> {
                when (next) {
                    AvailabilityAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("availability agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((attempts.toDouble() - ratio) * 100) / 100

    fun snapshot(): AvailabilityAgreement {
        return AvailabilityAgreement(
            code = code,
            label = label,
            lane = lane,
            odometer = odometer,
            ratio = ratio,
            dock = dock,
            attempts = attempts,
            region = region,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a availability agreement in status proposed. */
fun newAvailabilityAgreement(code: String, label: String): AvailabilityAgreement {
    return AvailabilityAgreement(
        code = code,
        label = label,
        lane = "",
        odometer = 0,
        ratio = 0.0,
        dock = "",
        attempts = 0,
        region = "",
        routeCode = "",
        status = AvailabilityAgreementStatus.PROPOSED,
    )
}
