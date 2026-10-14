package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy cutoff adjustment records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a legacy cutoff adjustment is in its life cycle. */
enum class LegacyCutoffAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy cutoff adjustment. Its code is unique within a tenant. */
data class LegacyCutoffAdjustment(
    var code: String,
    var title: String,
    var sequence: Int,
    var depot: String,
    var revision: Int,
    var status: LegacyCutoffAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCutoffAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCutoffAdjustmentStatus): Boolean {
        when (status) {
            LegacyCutoffAdjustmentStatus.NEW -> return next == LegacyCutoffAdjustmentStatus.FAILED
            LegacyCutoffAdjustmentStatus.QUEUED -> return next == LegacyCutoffAdjustmentStatus.DONE || next == LegacyCutoffAdjustmentStatus.FAILED
            LegacyCutoffAdjustmentStatus.RUNNING -> {
                when (next) {
                    LegacyCutoffAdjustmentStatus.DONE, LegacyCutoffAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            LegacyCutoffAdjustmentStatus.DONE -> {
                when (next) {
                    LegacyCutoffAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCutoffAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy cutoff adjustment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedVolume(): Double = round((revision.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): LegacyCutoffAdjustment {
        return LegacyCutoffAdjustment(
            code = code,
            title = title,
            sequence = sequence,
            depot = depot,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy cutoff adjustment in status new. */
fun newLegacyCutoffAdjustment(code: String, title: String): LegacyCutoffAdjustment {
    return LegacyCutoffAdjustment(
        code = code,
        title = title,
        sequence = 0,
        depot = "",
        revision = 0,
        status = LegacyCutoffAdjustmentStatus.NEW,
    )
}
