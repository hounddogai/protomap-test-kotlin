package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tire target is in its life cycle. */
enum class RollingTireTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling tire target and its current status. */
data class RollingTireTarget(
    var code: String,
    var name: String,
    var currency: String,
    var source: String,
    var floorValue: Double,
    var pallets: Int,
    var createdAt: Int,
    var depot: String,
    var channel: String,
    var margin: Double,
    var endsAt: Int,
    var status: RollingTireTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTireTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTireTargetStatus): Boolean {
        when (status) {
            RollingTireTargetStatus.DRAFT -> return next == RollingTireTargetStatus.CLOSED
            RollingTireTargetStatus.PENDING -> {
                when (next) {
                    RollingTireTargetStatus.ACTIVE, RollingTireTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingTireTargetStatus.ACTIVE -> {
                when (next) {
                    RollingTireTargetStatus.SUSPENDED, RollingTireTargetStatus.CLOSED -> return true
                }
                return false
            }
            RollingTireTargetStatus.SUSPENDED -> {
                when (next) {
                    RollingTireTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTireTargetStatus) {
        if (!canMoveTo(next)) {
            error("rolling tire target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((margin + floorValue) * 100) / 100

    fun snapshot(): RollingTireTarget {
        return RollingTireTarget(
            code = code,
            name = name,
            currency = currency,
            source = source,
            floorValue = floorValue,
            pallets = pallets,
            createdAt = createdAt,
            depot = depot,
            channel = channel,
            margin = margin,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling tire target in status draft. */
fun newRollingTireTarget(code: String, name: String): RollingTireTarget {
    return RollingTireTarget(
        code = code,
        name = name,
        currency = "",
        source = "",
        floorValue = 0.0,
        pallets = 0,
        createdAt = 0,
        depot = "",
        channel = "",
        margin = 0.0,
        endsAt = 0,
        status = RollingTireTargetStatus.DRAFT,
    )
}
