package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery group is in its life cycle. */
enum class RedeliveryGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A redelivery group as the redelivery team keeps it. */
data class RedeliveryGroup(
    var code: String,
    var name: String,
    var batchSize: Int,
    var startsAt: Int,
    var amount: Double,
    var label: String,
    var trailerId: String,
    var discount: Double,
    var version: Int,
    var status: RedeliveryGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryGroupStatus): Boolean {
        when (status) {
            RedeliveryGroupStatus.DRAFT -> {
                when (next) {
                    RedeliveryGroupStatus.SUSPENDED, RedeliveryGroupStatus.CLOSED -> return true
                }
                return false
            }
            RedeliveryGroupStatus.PENDING -> return next == RedeliveryGroupStatus.SUSPENDED
            RedeliveryGroupStatus.ACTIVE -> return next == RedeliveryGroupStatus.CLOSED
            RedeliveryGroupStatus.SUSPENDED -> {
                when (next) {
                    RedeliveryGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryGroupStatus) {
        if (!canMoveTo(next)) {
            error("redelivery group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"redelivery group"} $code"

    fun projectedValue(): Double = round((batchSize.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryGroup {
        return RedeliveryGroup(
            code = code,
            name = name,
            batchSize = batchSize,
            startsAt = startsAt,
            amount = amount,
            label = label,
            trailerId = trailerId,
            discount = discount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery group in status draft. */
fun newRedeliveryGroup(code: String, name: String): RedeliveryGroup {
    return RedeliveryGroup(
        code = code,
        name = name,
        batchSize = 0,
        startsAt = 0,
        amount = 0.0,
        label = "",
        trailerId = "",
        discount = 0.0,
        version = 0,
        status = RedeliveryGroupStatus.DRAFT,
    )
}
