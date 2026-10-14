package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization score records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization score is in its life cycle. */
enum class UtilizationScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A utilization score. Its code is unique within a tenant. */
data class UtilizationScore(
    var code: String,
    var name: String,
    var phone: String,
    var currency: String,
    var startsAt: Int,
    var fuelLiters: Double,
    var category: String,
    var depot: String,
    var contactEmail: String,
    var pallets: Int,
    var carrier: String,
    var status: UtilizationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationScoreStatus): Boolean {
        when (status) {
            UtilizationScoreStatus.DRAFT -> return next == UtilizationScoreStatus.PENDING || next == UtilizationScoreStatus.CLOSED
            UtilizationScoreStatus.PENDING -> return next == UtilizationScoreStatus.ACTIVE
            UtilizationScoreStatus.ACTIVE -> return next == UtilizationScoreStatus.SUSPENDED || next == UtilizationScoreStatus.CLOSED
            UtilizationScoreStatus.SUSPENDED -> {
                when (next) {
                    UtilizationScoreStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationScoreStatus) {
        if (!canMoveTo(next)) {
            error("utilization score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((pallets.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): UtilizationScore {
        return UtilizationScore(
            code = code,
            name = name,
            phone = phone,
            currency = currency,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            category = category,
            depot = depot,
            contactEmail = contactEmail,
            pallets = pallets,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a utilization score in status draft. */
fun newUtilizationScore(code: String, name: String): UtilizationScore {
    return UtilizationScore(
        code = code,
        name = name,
        phone = "",
        currency = "",
        startsAt = 0,
        fuelLiters = 0.0,
        category = "",
        depot = "",
        contactEmail = "",
        pallets = 0,
        carrier = "",
        status = UtilizationScoreStatus.DRAFT,
    )
}
