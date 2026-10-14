package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient emission factor request is in its life cycle. */
enum class LenientEmissionFactorRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient emission factor request. Its code is unique within a tenant. */
data class LenientEmissionFactorRequest(
    var code: String,
    var name: String,
    var budget: Double,
    var attempts: Int,
    var description: String,
    var capacity: Int,
    var stopCount: Int,
    var status: LenientEmissionFactorRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientEmissionFactorRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientEmissionFactorRequestStatus): Boolean {
        when (status) {
            LenientEmissionFactorRequestStatus.PROPOSED -> {
                when (next) {
                    LenientEmissionFactorRequestStatus.APPROVED, LenientEmissionFactorRequestStatus.REJECTED -> return true
                }
                return false
            }
            LenientEmissionFactorRequestStatus.APPROVED -> return next == LenientEmissionFactorRequestStatus.REJECTED || next == LenientEmissionFactorRequestStatus.WITHDRAWN
            LenientEmissionFactorRequestStatus.REJECTED -> return next == LenientEmissionFactorRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientEmissionFactorRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient emission factor request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveTotal(): Double = round((capacity.toDouble() - budget) * 100) / 100

    fun snapshot(): LenientEmissionFactorRequest {
        return LenientEmissionFactorRequest(
            code = code,
            name = name,
            budget = budget,
            attempts = attempts,
            description = description,
            capacity = capacity,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient emission factor request in status proposed. */
fun newLenientEmissionFactorRequest(code: String, name: String): LenientEmissionFactorRequest {
    return LenientEmissionFactorRequest(
        code = code,
        name = name,
        budget = 0.0,
        attempts = 0,
        description = "",
        capacity = 0,
        stopCount = 0,
        status = LenientEmissionFactorRequestStatus.PROPOSED,
    )
}
