package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return variance is in its life cycle. */
enum class LeaseReturnVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lease return variance, with what the planners need to act on it. */
data class LeaseReturnVariance(
    var code: String,
    var name: String,
    var description: String,
    var amount: Double,
    var durationMinutes: Int,
    var timezone: String,
    var title: String,
    var retries: Int,
    var status: LeaseReturnVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnVarianceStatus): Boolean {
        when (status) {
            LeaseReturnVarianceStatus.CREATED -> {
                when (next) {
                    LeaseReturnVarianceStatus.RETIRED -> return true
                }
                return false
            }
            LeaseReturnVarianceStatus.VALIDATED -> {
                when (next) {
                    LeaseReturnVarianceStatus.RETIRED -> return true
                }
                return false
            }
            LeaseReturnVarianceStatus.PUBLISHED -> {
                when (next) {
                    LeaseReturnVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnVarianceStatus) {
        if (!canMoveTo(next)) {
            error("lease return variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((durationMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnVariance {
        return LeaseReturnVariance(
            code = code,
            name = name,
            description = description,
            amount = amount,
            durationMinutes = durationMinutes,
            timezone = timezone,
            title = title,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lease return variance in status created. */
fun newLeaseReturnVariance(code: String, name: String): LeaseReturnVariance {
    return LeaseReturnVariance(
        code = code,
        name = name,
        description = "",
        amount = 0.0,
        durationMinutes = 0,
        timezone = "",
        title = "",
        retries = 0,
        status = LeaseReturnVarianceStatus.CREATED,
    )
}
