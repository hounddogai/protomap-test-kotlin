package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered redelivery checkpoint is in its life cycle. */
enum class TieredRedeliveryCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered redelivery checkpoint as the redelivery team keeps it. */
data class TieredRedeliveryCheckpoint(
    var code: String,
    var label: String,
    var description: String,
    var minimum: Double,
    var category: String,
    var score: Double,
    var contactEmail: String,
    var status: TieredRedeliveryCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRedeliveryCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRedeliveryCheckpointStatus): Boolean {
        when (status) {
            TieredRedeliveryCheckpointStatus.OPEN -> {
                when (next) {
                    TieredRedeliveryCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredRedeliveryCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    TieredRedeliveryCheckpointStatus.RESOLVED, TieredRedeliveryCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            TieredRedeliveryCheckpointStatus.RESOLVED -> return next == TieredRedeliveryCheckpointStatus.REOPENED
            TieredRedeliveryCheckpointStatus.REOPENED -> return next == TieredRedeliveryCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredRedeliveryCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tiered redelivery checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered redelivery checkpoint"} $code"

    fun weightedTotal(): Double = round((score - minimum) * 100) / 100

    fun snapshot(): TieredRedeliveryCheckpoint {
        return TieredRedeliveryCheckpoint(
            code = code,
            label = label,
            description = description,
            minimum = minimum,
            category = category,
            score = score,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered redelivery checkpoint in status open. */
fun newTieredRedeliveryCheckpoint(code: String, label: String): TieredRedeliveryCheckpoint {
    return TieredRedeliveryCheckpoint(
        code = code,
        label = label,
        description = "",
        minimum = 0.0,
        category = "",
        score = 0.0,
        contactEmail = "",
        status = TieredRedeliveryCheckpointStatus.OPEN,
    )
}
