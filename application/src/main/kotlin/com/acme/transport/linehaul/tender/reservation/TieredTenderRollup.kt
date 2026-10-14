package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tender rollup is in its life cycle. */
enum class TieredTenderRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered tender rollup, with what the planners need to act on it. */
data class TieredTenderRollup(
    var code: String,
    var name: String,
    var score: Double,
    var batchSize: Int,
    var stopCount: Int,
    var status: TieredTenderRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTenderRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTenderRollupStatus): Boolean {
        when (status) {
            TieredTenderRollupStatus.OPEN -> {
                when (next) {
                    TieredTenderRollupStatus.RESOLVED, TieredTenderRollupStatus.REOPENED -> return true
                }
                return false
            }
            TieredTenderRollupStatus.IN_REVIEW -> return next == TieredTenderRollupStatus.RESOLVED || next == TieredTenderRollupStatus.ARCHIVED
            TieredTenderRollupStatus.RESOLVED -> {
                when (next) {
                    TieredTenderRollupStatus.REOPENED -> return true
                }
                return false
            }
            TieredTenderRollupStatus.REOPENED -> {
                when (next) {
                    TieredTenderRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTenderRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered tender rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round(score * batchSize.toDouble() * 100) / 100

    fun snapshot(): TieredTenderRollup {
        return TieredTenderRollup(
            code = code,
            name = name,
            score = score,
            batchSize = batchSize,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered tender rollup in status open. */
fun newTieredTenderRollup(code: String, name: String): TieredTenderRollup {
    return TieredTenderRollup(
        code = code,
        name = name,
        score = 0.0,
        batchSize = 0,
        stopCount = 0,
        status = TieredTenderRollupStatus.OPEN,
    )
}
