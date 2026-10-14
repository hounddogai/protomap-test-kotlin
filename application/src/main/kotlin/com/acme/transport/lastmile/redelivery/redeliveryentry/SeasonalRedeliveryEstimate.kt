package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.normalize

/** Where a seasonal redelivery estimate is in its life cycle. */
enum class SeasonalRedeliveryEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal redelivery estimate. Its code is unique within a tenant. */
data class SeasonalRedeliveryEstimate(
    var code: String,
    var label: String,
    var quantity: Int,
    var summary: String,
    var ownerId: String,
    var approved: Boolean,
    var status: SeasonalRedeliveryEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryEstimateStatus): Boolean {
        when (status) {
            SeasonalRedeliveryEstimateStatus.REQUESTED -> {
                when (next) {
                    SeasonalRedeliveryEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalRedeliveryEstimateStatus.GRANTED -> return next == SeasonalRedeliveryEstimateStatus.REVOKED || next == SeasonalRedeliveryEstimateStatus.EXPIRED
            SeasonalRedeliveryEstimateStatus.REVOKED -> return next == SeasonalRedeliveryEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal redelivery estimate"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): SeasonalRedeliveryEstimate {
        return SeasonalRedeliveryEstimate(
            code = code,
            label = label,
            quantity = quantity,
            summary = summary,
            ownerId = ownerId,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal redelivery estimate in status requested. */
fun newSeasonalRedeliveryEstimate(code: String, label: String): SeasonalRedeliveryEstimate {
    return SeasonalRedeliveryEstimate(
        code = code,
        label = label,
        quantity = 0,
        summary = "",
        ownerId = "",
        approved = false,
        status = SeasonalRedeliveryEstimateStatus.REQUESTED,
    )
}
