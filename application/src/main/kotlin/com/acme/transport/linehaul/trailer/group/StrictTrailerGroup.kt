package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict trailer group records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a strict trailer group is in its life cycle. */
enum class StrictTrailerGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict trailer group. Its code is unique within a tenant. */
data class StrictTrailerGroup(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var visible: Boolean,
    var driverId: String,
    var quantity: Int,
    var summary: String,
    var durationMinutes: Int,
    var minimum: Double,
    var loadKg: Double,
    var budget: Double,
    var status: StrictTrailerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerGroupStatus): Boolean {
        when (status) {
            StrictTrailerGroupStatus.OPEN -> {
                when (next) {
                    StrictTrailerGroupStatus.REOPENED -> return true
                }
                return false
            }
            StrictTrailerGroupStatus.IN_REVIEW -> return next == StrictTrailerGroupStatus.RESOLVED
            StrictTrailerGroupStatus.RESOLVED -> return next == StrictTrailerGroupStatus.REOPENED || next == StrictTrailerGroupStatus.ARCHIVED
            StrictTrailerGroupStatus.REOPENED -> {
                when (next) {
                    StrictTrailerGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTrailerGroupStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict trailer group"} $code"

    fun adjustedValue(): Double = round(budget * loadKg * 100) / 100

    fun snapshot(): StrictTrailerGroup {
        return StrictTrailerGroup(
            code = code,
            name = name,
            expiresAt = expiresAt,
            visible = visible,
            driverId = driverId,
            quantity = quantity,
            summary = summary,
            durationMinutes = durationMinutes,
            minimum = minimum,
            loadKg = loadKg,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict trailer group in status open. */
fun newStrictTrailerGroup(code: String, name: String): StrictTrailerGroup {
    return StrictTrailerGroup(
        code = code,
        name = name,
        expiresAt = 0,
        visible = false,
        driverId = "",
        quantity = 0,
        summary = "",
        durationMinutes = 0,
        minimum = 0.0,
        loadKg = 0.0,
        budget = 0.0,
        status = StrictTrailerGroupStatus.OPEN,
    )
}
