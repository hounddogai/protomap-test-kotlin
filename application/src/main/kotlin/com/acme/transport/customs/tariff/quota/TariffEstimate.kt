package com.acme.transport.customs.tariff.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff estimate is in its life cycle. */
enum class TariffEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff estimate. Its code is unique within a tenant. */
data class TariffEstimate(
    var code: String,
    var name: String,
    var region: String,
    var ceiling: Double,
    var archived: Boolean,
    var contactEmail: String,
    var threshold: Double,
    var amount: Double,
    var stopCount: Int,
    var status: TariffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffEstimateStatus): Boolean {
        when (status) {
            TariffEstimateStatus.PLANNED -> {
                when (next) {
                    TariffEstimateStatus.SCHEDULED, TariffEstimateStatus.COMPLETED -> return true
                }
                return false
            }
            TariffEstimateStatus.SCHEDULED -> return next == TariffEstimateStatus.IN_PROGRESS
            TariffEstimateStatus.IN_PROGRESS -> return next == TariffEstimateStatus.COMPLETED
            TariffEstimateStatus.COMPLETED -> {
                when (next) {
                    TariffEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("tariff estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((amount - ceiling) * 100) / 100

    fun snapshot(): TariffEstimate {
        return TariffEstimate(
            code = code,
            name = name,
            region = region,
            ceiling = ceiling,
            archived = archived,
            contactEmail = contactEmail,
            threshold = threshold,
            amount = amount,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tariff estimate in status planned. */
fun newTariffEstimate(code: String, name: String): TariffEstimate {
    return TariffEstimate(
        code = code,
        name = name,
        region = "",
        ceiling = 0.0,
        archived = false,
        contactEmail = "",
        threshold = 0.0,
        amount = 0.0,
        stopCount = 0,
        status = TariffEstimateStatus.PLANNED,
    )
}
