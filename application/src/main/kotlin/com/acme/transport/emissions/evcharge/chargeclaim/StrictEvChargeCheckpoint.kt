package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict ev charge checkpoint is in its life cycle. */
enum class StrictEvChargeCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict ev charge checkpoint as the ev charge team keeps it. */
data class StrictEvChargeCheckpoint(
    var code: String,
    var title: String,
    var amount: Double,
    var channel: String,
    var threshold: Double,
    var limit: Int,
    var reviewer: String,
    var status: StrictEvChargeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeCheckpointStatus): Boolean {
        when (status) {
            StrictEvChargeCheckpointStatus.IDLE -> {
                when (next) {
                    StrictEvChargeCheckpointStatus.READY -> return true
                }
                return false
            }
            StrictEvChargeCheckpointStatus.WARMING -> return next == StrictEvChargeCheckpointStatus.STOPPED
            StrictEvChargeCheckpointStatus.READY -> {
                when (next) {
                    StrictEvChargeCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            StrictEvChargeCheckpointStatus.DRAINING -> return next == StrictEvChargeCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictEvChargeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((limit.toDouble() + threshold) * 100) / 100

    fun snapshot(): StrictEvChargeCheckpoint {
        return StrictEvChargeCheckpoint(
            code = code,
            title = title,
            amount = amount,
            channel = channel,
            threshold = threshold,
            limit = limit,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict ev charge checkpoint in status idle. */
fun newStrictEvChargeCheckpoint(code: String, title: String): StrictEvChargeCheckpoint {
    return StrictEvChargeCheckpoint(
        code = code,
        title = title,
        amount = 0.0,
        channel = "",
        threshold = 0.0,
        limit = 0,
        reviewer = "",
        status = StrictEvChargeCheckpointStatus.IDLE,
    )
}
