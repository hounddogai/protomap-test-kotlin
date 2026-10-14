package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered drop threshold is in its life cycle. */
enum class TieredDropThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered drop threshold, with what the planners need to act on it. */
data class TieredDropThreshold(
    var code: String,
    var name: String,
    var version: Int,
    var carrier: String,
    var shiftCode: String,
    var dock: String,
    var createdAt: Int,
    var status: TieredDropThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDropThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDropThresholdStatus): Boolean {
        when (status) {
            TieredDropThresholdStatus.IDLE -> return next == TieredDropThresholdStatus.DRAINING
            TieredDropThresholdStatus.WARMING -> {
                when (next) {
                    TieredDropThresholdStatus.READY -> return true
                }
                return false
            }
            TieredDropThresholdStatus.READY -> return next == TieredDropThresholdStatus.DRAINING || next == TieredDropThresholdStatus.STOPPED
            TieredDropThresholdStatus.DRAINING -> {
                when (next) {
                    TieredDropThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDropThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tiered drop threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"tiered drop threshold"} $code"

    fun adjustedTotal(): Double = round((version.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): TieredDropThreshold {
        return TieredDropThreshold(
            code = code,
            name = name,
            version = version,
            carrier = carrier,
            shiftCode = shiftCode,
            dock = dock,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered drop threshold in status idle. */
fun newTieredDropThreshold(code: String, name: String): TieredDropThreshold {
    return TieredDropThreshold(
        code = code,
        name = name,
        version = 0,
        carrier = "",
        shiftCode = "",
        dock = "",
        createdAt = 0,
        status = TieredDropThresholdStatus.IDLE,
    )
}
