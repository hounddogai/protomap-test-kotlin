package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.text.normalize

/** Where a tiered rest break rollup is in its life cycle. */
enum class TieredRestBreakRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered rest break rollup and its current status. */
data class TieredRestBreakRollup(
    var code: String,
    var label: String,
    var pallets: Int,
    var name: String,
    var externalRef: String,
    var country: String,
    var visible: Boolean,
    var status: TieredRestBreakRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRestBreakRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRestBreakRollupStatus): Boolean {
        when (status) {
            TieredRestBreakRollupStatus.IDLE -> {
                when (next) {
                    TieredRestBreakRollupStatus.STOPPED -> return true
                }
                return false
            }
            TieredRestBreakRollupStatus.WARMING -> return next == TieredRestBreakRollupStatus.READY || next == TieredRestBreakRollupStatus.DRAINING
            TieredRestBreakRollupStatus.READY -> return next == TieredRestBreakRollupStatus.DRAINING
            TieredRestBreakRollupStatus.DRAINING -> {
                when (next) {
                    TieredRestBreakRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRestBreakRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered rest break rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): TieredRestBreakRollup {
        return TieredRestBreakRollup(
            code = code,
            label = label,
            pallets = pallets,
            name = name,
            externalRef = externalRef,
            country = country,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered rest break rollup in status idle. */
fun newTieredRestBreakRollup(code: String, label: String): TieredRestBreakRollup {
    return TieredRestBreakRollup(
        code = code,
        label = label,
        pallets = 0,
        name = "",
        externalRef = "",
        country = "",
        visible = false,
        status = TieredRestBreakRollupStatus.IDLE,
    )
}
