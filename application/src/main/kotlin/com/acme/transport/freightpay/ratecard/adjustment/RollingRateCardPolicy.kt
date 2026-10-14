package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling rate card policy is in its life cycle. */
enum class RollingRateCardPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling rate card policy as the rate card team keeps it. */
data class RollingRateCardPolicy(
    var code: String,
    var name: String,
    var vehicleId: String,
    var revision: Int,
    var createdAt: Int,
    var rate: Double,
    var graceDays: Int,
    var updatedAt: Int,
    var currency: String,
    var contactEmail: String,
    var status: RollingRateCardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRateCardPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRateCardPolicyStatus): Boolean {
        when (status) {
            RollingRateCardPolicyStatus.DRAFT -> return next == RollingRateCardPolicyStatus.ACTIVE || next == RollingRateCardPolicyStatus.CLOSED
            RollingRateCardPolicyStatus.PENDING -> return next == RollingRateCardPolicyStatus.SUSPENDED
            RollingRateCardPolicyStatus.ACTIVE -> {
                when (next) {
                    RollingRateCardPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingRateCardPolicyStatus.SUSPENDED -> return next == RollingRateCardPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingRateCardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling rate card policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((revision.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): RollingRateCardPolicy {
        return RollingRateCardPolicy(
            code = code,
            name = name,
            vehicleId = vehicleId,
            revision = revision,
            createdAt = createdAt,
            rate = rate,
            graceDays = graceDays,
            updatedAt = updatedAt,
            currency = currency,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling rate card policy in status draft. */
fun newRollingRateCardPolicy(code: String, name: String): RollingRateCardPolicy {
    return RollingRateCardPolicy(
        code = code,
        name = name,
        vehicleId = "",
        revision = 0,
        createdAt = 0,
        rate = 0.0,
        graceDays = 0,
        updatedAt = 0,
        currency = "",
        contactEmail = "",
        status = RollingRateCardPolicyStatus.DRAFT,
    )
}
