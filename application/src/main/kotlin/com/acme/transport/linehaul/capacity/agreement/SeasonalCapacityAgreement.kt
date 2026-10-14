package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal capacity agreement records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a seasonal capacity agreement is in its life cycle. */
enum class SeasonalCapacityAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal capacity agreement as the capacity team keeps it. */
data class SeasonalCapacityAgreement(
    var code: String,
    var name: String,
    var attempts: Int,
    var amount: Double,
    var cost: Double,
    var startsAt: Int,
    var status: SeasonalCapacityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCapacityAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCapacityAgreementStatus): Boolean {
        when (status) {
            SeasonalCapacityAgreementStatus.OPEN -> {
                when (next) {
                    SeasonalCapacityAgreementStatus.IN_REVIEW -> return true
                }
                return false
            }
            SeasonalCapacityAgreementStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalCapacityAgreementStatus.RESOLVED, SeasonalCapacityAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalCapacityAgreementStatus.RESOLVED -> return next == SeasonalCapacityAgreementStatus.ARCHIVED
            SeasonalCapacityAgreementStatus.REOPENED -> {
                when (next) {
                    SeasonalCapacityAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCapacityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("seasonal capacity agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((startsAt.toDouble() + cost) * 100) / 100

    fun snapshot(): SeasonalCapacityAgreement {
        return SeasonalCapacityAgreement(
            code = code,
            name = name,
            attempts = attempts,
            amount = amount,
            cost = cost,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal capacity agreement in status open. */
fun newSeasonalCapacityAgreement(code: String, name: String): SeasonalCapacityAgreement {
    return SeasonalCapacityAgreement(
        code = code,
        name = name,
        attempts = 0,
        amount = 0.0,
        cost = 0.0,
        startsAt = 0,
        status = SeasonalCapacityAgreementStatus.OPEN,
    )
}
