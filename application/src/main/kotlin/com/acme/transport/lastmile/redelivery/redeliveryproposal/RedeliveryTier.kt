package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery tier is in its life cycle. */
enum class RedeliveryTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A redelivery tier as the redelivery team keeps it. */
data class RedeliveryTier(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var source: String,
    var windowDays: Int,
    var status: RedeliveryTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryTierStatus): Boolean {
        when (status) {
            RedeliveryTierStatus.REQUESTED -> {
                when (next) {
                    RedeliveryTierStatus.GRANTED, RedeliveryTierStatus.EXPIRED -> return true
                }
                return false
            }
            RedeliveryTierStatus.GRANTED -> return next == RedeliveryTierStatus.REVOKED
            RedeliveryTierStatus.REVOKED -> {
                when (next) {
                    RedeliveryTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryTierStatus) {
        if (!canMoveTo(next)) {
            error("redelivery tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"redelivery tier"} $code"

    fun netValue(): Double = round((fuelLiters - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryTier {
        return RedeliveryTier(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            source = source,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a redelivery tier in status requested. */
fun newRedeliveryTier(code: String, title: String): RedeliveryTier {
    return RedeliveryTier(
        code = code,
        title = title,
        fuelLiters = 0.0,
        source = "",
        windowDays = 0,
        status = RedeliveryTierStatus.REQUESTED,
    )
}
