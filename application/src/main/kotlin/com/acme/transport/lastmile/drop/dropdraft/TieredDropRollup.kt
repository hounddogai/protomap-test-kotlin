package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered drop rollup is in its life cycle. */
enum class TieredDropRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered drop rollup and its current status. */
data class TieredDropRollup(
    var code: String,
    var label: String,
    var driverId: String,
    var floorValue: Double,
    var attempts: Int,
    var ceiling: Double,
    var status: TieredDropRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDropRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDropRollupStatus): Boolean {
        when (status) {
            TieredDropRollupStatus.IDLE -> {
                when (next) {
                    TieredDropRollupStatus.WARMING, TieredDropRollupStatus.DRAINING -> return true
                }
                return false
            }
            TieredDropRollupStatus.WARMING -> return next == TieredDropRollupStatus.READY || next == TieredDropRollupStatus.STOPPED
            TieredDropRollupStatus.READY -> {
                when (next) {
                    TieredDropRollupStatus.STOPPED -> return true
                }
                return false
            }
            TieredDropRollupStatus.DRAINING -> {
                when (next) {
                    TieredDropRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDropRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered drop rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round(floorValue * attempts.toDouble() * 100) / 100

    fun snapshot(): TieredDropRollup {
        return TieredDropRollup(
            code = code,
            label = label,
            driverId = driverId,
            floorValue = floorValue,
            attempts = attempts,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered drop rollup in status idle. */
fun newTieredDropRollup(code: String, label: String): TieredDropRollup {
    return TieredDropRollup(
        code = code,
        label = label,
        driverId = "",
        floorValue = 0.0,
        attempts = 0,
        ceiling = 0.0,
        status = TieredDropRollupStatus.IDLE,
    )
}
