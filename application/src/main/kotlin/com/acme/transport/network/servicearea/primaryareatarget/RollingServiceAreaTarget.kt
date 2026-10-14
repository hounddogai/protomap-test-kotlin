package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling service area target records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a rolling service area target is in its life cycle. */
enum class RollingServiceAreaTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rolling service area target and its current status. */
data class RollingServiceAreaTarget(
    var code: String,
    var name: String,
    var sequence: Int,
    var priority: Int,
    var fuelLiters: Double,
    var currency: String,
    var dock: String,
    var status: RollingServiceAreaTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingServiceAreaTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingServiceAreaTargetStatus): Boolean {
        when (status) {
            RollingServiceAreaTargetStatus.CREATED -> {
                when (next) {
                    RollingServiceAreaTargetStatus.PUBLISHED, RollingServiceAreaTargetStatus.RETIRED -> return true
                }
                return false
            }
            RollingServiceAreaTargetStatus.VALIDATED -> {
                when (next) {
                    RollingServiceAreaTargetStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingServiceAreaTargetStatus.PUBLISHED -> return next == RollingServiceAreaTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingServiceAreaTargetStatus) {
        if (!canMoveTo(next)) {
            error("rolling service area target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((fuelLiters + sequence.toDouble()) * 100) / 100

    fun snapshot(): RollingServiceAreaTarget {
        return RollingServiceAreaTarget(
            code = code,
            name = name,
            sequence = sequence,
            priority = priority,
            fuelLiters = fuelLiters,
            currency = currency,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling service area target in status created. */
fun newRollingServiceAreaTarget(code: String, name: String): RollingServiceAreaTarget {
    return RollingServiceAreaTarget(
        code = code,
        name = name,
        sequence = 0,
        priority = 0,
        fuelLiters = 0.0,
        currency = "",
        dock = "",
        status = RollingServiceAreaTargetStatus.CREATED,
    )
}
