package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient tariff request records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a lenient tariff request is in its life cycle. */
enum class LenientTariffRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient tariff request as the tariff team keeps it. */
data class LenientTariffRequest(
    var code: String,
    var name: String,
    var threshold: Double,
    var updatedAt: Int,
    var ceiling: Double,
    var createdAt: Int,
    var version: Int,
    var status: LenientTariffRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTariffRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTariffRequestStatus): Boolean {
        when (status) {
            LenientTariffRequestStatus.PLANNED -> return next == LenientTariffRequestStatus.SCHEDULED
            LenientTariffRequestStatus.SCHEDULED -> return next == LenientTariffRequestStatus.CANCELLED
            LenientTariffRequestStatus.IN_PROGRESS -> return next == LenientTariffRequestStatus.COMPLETED || next == LenientTariffRequestStatus.CANCELLED
            LenientTariffRequestStatus.COMPLETED -> {
                when (next) {
                    LenientTariffRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTariffRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient tariff request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): LenientTariffRequest {
        return LenientTariffRequest(
            code = code,
            name = name,
            threshold = threshold,
            updatedAt = updatedAt,
            ceiling = ceiling,
            createdAt = createdAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient tariff request in status planned. */
fun newLenientTariffRequest(code: String, name: String): LenientTariffRequest {
    return LenientTariffRequest(
        code = code,
        name = name,
        threshold = 0.0,
        updatedAt = 0,
        ceiling = 0.0,
        createdAt = 0,
        version = 0,
        status = LenientTariffRequestStatus.PLANNED,
    )
}
