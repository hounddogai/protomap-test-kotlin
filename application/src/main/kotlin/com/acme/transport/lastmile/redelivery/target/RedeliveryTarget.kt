package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery target records of lastmile's redelivery work, and the rules, stores, and calculations around
// them.

/** Where a redelivery target is in its life cycle. */
enum class RedeliveryTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A redelivery target as the redelivery team keeps it. */
data class RedeliveryTarget(
    var code: String,
    var name: String,
    var currency: String,
    var limit: Int,
    var discount: Double,
    var capacity: Int,
    var externalRef: String,
    var durationMinutes: Int,
    var visible: Boolean,
    var country: String,
    var status: RedeliveryTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryTargetStatus): Boolean {
        when (status) {
            RedeliveryTargetStatus.REQUESTED -> {
                when (next) {
                    RedeliveryTargetStatus.REVOKED, RedeliveryTargetStatus.EXPIRED -> return true
                }
                return false
            }
            RedeliveryTargetStatus.GRANTED -> {
                when (next) {
                    RedeliveryTargetStatus.EXPIRED -> return true
                }
                return false
            }
            RedeliveryTargetStatus.REVOKED -> {
                when (next) {
                    RedeliveryTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryTargetStatus) {
        if (!canMoveTo(next)) {
            error("redelivery target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round(durationMinutes.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): RedeliveryTarget {
        return RedeliveryTarget(
            code = code,
            name = name,
            currency = currency,
            limit = limit,
            discount = discount,
            capacity = capacity,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            visible = visible,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a redelivery target in status requested. */
fun newRedeliveryTarget(code: String, name: String): RedeliveryTarget {
    return RedeliveryTarget(
        code = code,
        name = name,
        currency = "",
        limit = 0,
        discount = 0.0,
        capacity = 0,
        externalRef = "",
        durationMinutes = 0,
        visible = false,
        country = "",
        status = RedeliveryTargetStatus.REQUESTED,
    )
}
