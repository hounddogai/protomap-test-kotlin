package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling rest break rollup records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rolling rest break rollup is in its life cycle. */
enum class RollingRestBreakRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling rest break rollup as the rest break team keeps it. */
data class RollingRestBreakRollup(
    var code: String,
    var title: String,
    var archived: Boolean,
    var dock: String,
    var ceiling: Double,
    var phone: String,
    var active: Boolean,
    var country: String,
    var floorValue: Double,
    var depot: String,
    var discount: Double,
    var status: RollingRestBreakRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRestBreakRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRestBreakRollupStatus): Boolean {
        when (status) {
            RollingRestBreakRollupStatus.DRAFT -> {
                when (next) {
                    RollingRestBreakRollupStatus.CLOSED -> return true
                }
                return false
            }
            RollingRestBreakRollupStatus.PENDING -> {
                when (next) {
                    RollingRestBreakRollupStatus.CLOSED -> return true
                }
                return false
            }
            RollingRestBreakRollupStatus.ACTIVE -> {
                when (next) {
                    RollingRestBreakRollupStatus.CLOSED -> return true
                }
                return false
            }
            RollingRestBreakRollupStatus.SUSPENDED -> return next == RollingRestBreakRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingRestBreakRollupStatus) {
        if (!canMoveTo(next)) {
            error("rolling rest break rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round(floorValue * ceiling * 100) / 100

    fun snapshot(): RollingRestBreakRollup {
        return RollingRestBreakRollup(
            code = code,
            title = title,
            archived = archived,
            dock = dock,
            ceiling = ceiling,
            phone = phone,
            active = active,
            country = country,
            floorValue = floorValue,
            depot = depot,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling rest break rollup in status draft. */
fun newRollingRestBreakRollup(code: String, title: String): RollingRestBreakRollup {
    return RollingRestBreakRollup(
        code = code,
        title = title,
        archived = false,
        dock = "",
        ceiling = 0.0,
        phone = "",
        active = false,
        country = "",
        floorValue = 0.0,
        depot = "",
        discount = 0.0,
        status = RollingRestBreakRollupStatus.DRAFT,
    )
}
