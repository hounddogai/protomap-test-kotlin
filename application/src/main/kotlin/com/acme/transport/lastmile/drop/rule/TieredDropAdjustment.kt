package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered drop adjustment is in its life cycle. */
enum class TieredDropAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tiered drop adjustment, with what the planners need to act on it. */
data class TieredDropAdjustment(
    var code: String,
    var name: String,
    var ratio: Double,
    var batchSize: Int,
    var description: String,
    var status: TieredDropAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDropAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDropAdjustmentStatus): Boolean {
        when (status) {
            TieredDropAdjustmentStatus.NEW -> {
                when (next) {
                    TieredDropAdjustmentStatus.QUEUED, TieredDropAdjustmentStatus.DONE -> return true
                }
                return false
            }
            TieredDropAdjustmentStatus.QUEUED -> return next == TieredDropAdjustmentStatus.DONE || next == TieredDropAdjustmentStatus.FAILED
            TieredDropAdjustmentStatus.RUNNING -> {
                when (next) {
                    TieredDropAdjustmentStatus.DONE -> return true
                }
                return false
            }
            TieredDropAdjustmentStatus.DONE -> {
                when (next) {
                    TieredDropAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDropAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered drop adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((batchSize.toDouble() - ratio) * 100) / 100

    fun snapshot(): TieredDropAdjustment {
        return TieredDropAdjustment(
            code = code,
            name = name,
            ratio = ratio,
            batchSize = batchSize,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered drop adjustment in status new. */
fun newTieredDropAdjustment(code: String, name: String): TieredDropAdjustment {
    return TieredDropAdjustment(
        code = code,
        name = name,
        ratio = 0.0,
        batchSize = 0,
        description = "",
        status = TieredDropAdjustmentStatus.NEW,
    )
}
