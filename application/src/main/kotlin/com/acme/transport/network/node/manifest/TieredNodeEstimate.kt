package com.acme.transport.network.node.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered node estimate is in its life cycle. */
enum class TieredNodeEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tiered node estimate as the node team keeps it. */
data class TieredNodeEstimate(
    var code: String,
    var title: String,
    var sequence: Int,
    var contactEmail: String,
    var shiftCode: String,
    var weight: Double,
    var status: TieredNodeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNodeEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNodeEstimateStatus): Boolean {
        when (status) {
            TieredNodeEstimateStatus.DRAFT -> return next == TieredNodeEstimateStatus.ACTIVE
            TieredNodeEstimateStatus.PENDING -> {
                when (next) {
                    TieredNodeEstimateStatus.SUSPENDED, TieredNodeEstimateStatus.CLOSED -> return true
                }
                return false
            }
            TieredNodeEstimateStatus.ACTIVE -> {
                when (next) {
                    TieredNodeEstimateStatus.SUSPENDED, TieredNodeEstimateStatus.CLOSED -> return true
                }
                return false
            }
            TieredNodeEstimateStatus.SUSPENDED -> return next == TieredNodeEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredNodeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("tiered node estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((sequence.toDouble() + weight) * 100) / 100

    fun snapshot(): TieredNodeEstimate {
        return TieredNodeEstimate(
            code = code,
            title = title,
            sequence = sequence,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered node estimate in status draft. */
fun newTieredNodeEstimate(code: String, title: String): TieredNodeEstimate {
    return TieredNodeEstimate(
        code = code,
        title = title,
        sequence = 0,
        contactEmail = "",
        shiftCode = "",
        weight = 0.0,
        status = TieredNodeEstimateStatus.DRAFT,
    )
}
