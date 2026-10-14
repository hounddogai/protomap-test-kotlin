package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize

/** Where a pending rest break rollup is in its life cycle. */
enum class PendingRestBreakRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending rest break rollup. Its code is unique within a tenant. */
data class PendingRestBreakRollup(
    var code: String,
    var name: String,
    var locale: String,
    var weight: Double,
    var carrier: String,
    var status: PendingRestBreakRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRestBreakRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRestBreakRollupStatus): Boolean {
        when (status) {
            PendingRestBreakRollupStatus.IDLE -> return next == PendingRestBreakRollupStatus.READY
            PendingRestBreakRollupStatus.WARMING -> {
                when (next) {
                    PendingRestBreakRollupStatus.READY -> return true
                }
                return false
            }
            PendingRestBreakRollupStatus.READY -> {
                when (next) {
                    PendingRestBreakRollupStatus.STOPPED -> return true
                }
                return false
            }
            PendingRestBreakRollupStatus.DRAINING -> {
                when (next) {
                    PendingRestBreakRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRestBreakRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending rest break rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending rest break rollup"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): PendingRestBreakRollup {
        return PendingRestBreakRollup(
            code = code,
            name = name,
            locale = locale,
            weight = weight,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending rest break rollup in status idle. */
fun newPendingRestBreakRollup(code: String, name: String): PendingRestBreakRollup {
    return PendingRestBreakRollup(
        code = code,
        name = name,
        locale = "",
        weight = 0.0,
        carrier = "",
        status = PendingRestBreakRollupStatus.IDLE,
    )
}
