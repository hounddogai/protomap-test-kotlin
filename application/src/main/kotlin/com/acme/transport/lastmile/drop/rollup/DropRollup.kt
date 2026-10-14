package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop rollup records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop rollup is in its life cycle. */
enum class DropRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a drop rollup and its current status. */
data class DropRollup(
    var code: String,
    var name: String,
    var limit: Int,
    var tags: MutableList<String>,
    var weight: Double,
    var rate: Double,
    var startsAt: Int,
    var margin: Double,
    var fuelLiters: Double,
    var status: DropRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropRollupStatus): Boolean {
        when (status) {
            DropRollupStatus.OPEN -> {
                when (next) {
                    DropRollupStatus.RESOLVED -> return true
                }
                return false
            }
            DropRollupStatus.IN_REVIEW -> return next == DropRollupStatus.REOPENED
            DropRollupStatus.RESOLVED -> {
                when (next) {
                    DropRollupStatus.REOPENED -> return true
                }
                return false
            }
            DropRollupStatus.REOPENED -> return next == DropRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropRollupStatus) {
        if (!canMoveTo(next)) {
            error("drop rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((startsAt.toDouble() - rate) * 100) / 100

    fun snapshot(): DropRollup {
        return DropRollup(
            code = code,
            name = name,
            limit = limit,
            tags = tags,
            weight = weight,
            rate = rate,
            startsAt = startsAt,
            margin = margin,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a drop rollup in status open. */
fun newDropRollup(code: String, name: String): DropRollup {
    return DropRollup(
        code = code,
        name = name,
        limit = 0,
        tags = mutableListOf<String>(),
        weight = 0.0,
        rate = 0.0,
        startsAt = 0,
        margin = 0.0,
        fuelLiters = 0.0,
        status = DropRollupStatus.OPEN,
    )
}
