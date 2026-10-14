package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback emission factor request is in its life cycle. */
enum class FallbackEmissionFactorRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback emission factor request, with what the planners need to act on it. */
data class FallbackEmissionFactorRequest(
    var code: String,
    var title: String,
    var vehicleId: String,
    var phone: String,
    var version: Int,
    var discount: Double,
    var durationMinutes: Int,
    var summary: String,
    var driverId: String,
    var status: FallbackEmissionFactorRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEmissionFactorRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEmissionFactorRequestStatus): Boolean {
        when (status) {
            FallbackEmissionFactorRequestStatus.PROPOSED -> {
                when (next) {
                    FallbackEmissionFactorRequestStatus.REJECTED, FallbackEmissionFactorRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackEmissionFactorRequestStatus.APPROVED -> return next == FallbackEmissionFactorRequestStatus.REJECTED || next == FallbackEmissionFactorRequestStatus.WITHDRAWN
            FallbackEmissionFactorRequestStatus.REJECTED -> {
                when (next) {
                    FallbackEmissionFactorRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEmissionFactorRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback emission factor request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"fallback emission factor request"} $code"

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): FallbackEmissionFactorRequest {
        return FallbackEmissionFactorRequest(
            code = code,
            title = title,
            vehicleId = vehicleId,
            phone = phone,
            version = version,
            discount = discount,
            durationMinutes = durationMinutes,
            summary = summary,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback emission factor request in status proposed. */
fun newFallbackEmissionFactorRequest(code: String, title: String): FallbackEmissionFactorRequest {
    return FallbackEmissionFactorRequest(
        code = code,
        title = title,
        vehicleId = "",
        phone = "",
        version = 0,
        discount = 0.0,
        durationMinutes = 0,
        summary = "",
        driverId = "",
        status = FallbackEmissionFactorRequestStatus.PROPOSED,
    )
}
