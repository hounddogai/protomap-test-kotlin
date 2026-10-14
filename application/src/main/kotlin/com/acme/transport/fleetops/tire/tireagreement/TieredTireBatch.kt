package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tire batch is in its life cycle. */
enum class TieredTireBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered tire batch, with what the planners need to act on it. */
data class TieredTireBatch(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var depot: String,
    var pinned: Boolean,
    var amount: Double,
    var label: String,
    var segment: String,
    var status: TieredTireBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTireBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTireBatchStatus): Boolean {
        when (status) {
            TieredTireBatchStatus.PLANNED -> {
                when (next) {
                    TieredTireBatchStatus.IN_PROGRESS, TieredTireBatchStatus.CANCELLED -> return true
                }
                return false
            }
            TieredTireBatchStatus.SCHEDULED -> return next == TieredTireBatchStatus.IN_PROGRESS || next == TieredTireBatchStatus.CANCELLED
            TieredTireBatchStatus.IN_PROGRESS -> {
                when (next) {
                    TieredTireBatchStatus.COMPLETED, TieredTireBatchStatus.CANCELLED -> return true
                }
                return false
            }
            TieredTireBatchStatus.COMPLETED -> return next == TieredTireBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredTireBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered tire batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedVolume(): Double = round((amount + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredTireBatch {
        return TieredTireBatch(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            depot = depot,
            pinned = pinned,
            amount = amount,
            label = label,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered tire batch in status planned. */
fun newTieredTireBatch(code: String, title: String): TieredTireBatch {
    return TieredTireBatch(
        code = code,
        title = title,
        etaMinutes = 0,
        depot = "",
        pinned = false,
        amount = 0.0,
        label = "",
        segment = "",
        status = TieredTireBatchStatus.PLANNED,
    )
}
