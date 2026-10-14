package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul rollup is in its life cycle. */
enum class BackhaulRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One backhaul rollup, with what the planners need to act on it. */
data class BackhaulRollup(
    var code: String,
    var name: String,
    var discount: Double,
    var ceiling: Double,
    var segment: String,
    var pallets: Int,
    var odometer: Int,
    var active: Boolean,
    var summary: String,
    var notes: String,
    var status: BackhaulRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulRollupStatus): Boolean {
        when (status) {
            BackhaulRollupStatus.DRAFT -> {
                when (next) {
                    BackhaulRollupStatus.SUSPENDED -> return true
                }
                return false
            }
            BackhaulRollupStatus.PENDING -> return next == BackhaulRollupStatus.CLOSED
            BackhaulRollupStatus.ACTIVE -> {
                when (next) {
                    BackhaulRollupStatus.SUSPENDED -> return true
                }
                return false
            }
            BackhaulRollupStatus.SUSPENDED -> {
                when (next) {
                    BackhaulRollupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulRollupStatus) {
        if (!canMoveTo(next)) {
            error("backhaul rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((ceiling + odometer.toDouble()) * 100) / 100

    fun snapshot(): BackhaulRollup {
        return BackhaulRollup(
            code = code,
            name = name,
            discount = discount,
            ceiling = ceiling,
            segment = segment,
            pallets = pallets,
            odometer = odometer,
            active = active,
            summary = summary,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a backhaul rollup in status draft. */
fun newBackhaulRollup(code: String, name: String): BackhaulRollup {
    return BackhaulRollup(
        code = code,
        name = name,
        discount = 0.0,
        ceiling = 0.0,
        segment = "",
        pallets = 0,
        odometer = 0,
        active = false,
        summary = "",
        notes = "",
        status = BackhaulRollupStatus.DRAFT,
    )
}
