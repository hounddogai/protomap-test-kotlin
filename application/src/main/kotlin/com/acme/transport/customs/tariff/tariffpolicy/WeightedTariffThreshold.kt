package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tariff threshold is in its life cycle. */
enum class WeightedTariffThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted tariff threshold. Its code is unique within a tenant. */
data class WeightedTariffThreshold(
    var code: String,
    var label: String,
    var retries: Int,
    var startsAt: Int,
    var approved: Boolean,
    var floorValue: Double,
    var archived: Boolean,
    var pallets: Int,
    var status: WeightedTariffThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTariffThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTariffThresholdStatus): Boolean {
        when (status) {
            WeightedTariffThresholdStatus.PLANNED -> {
                when (next) {
                    WeightedTariffThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTariffThresholdStatus.SCHEDULED -> return next == WeightedTariffThresholdStatus.CANCELLED
            WeightedTariffThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedTariffThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTariffThresholdStatus.COMPLETED -> {
                when (next) {
                    WeightedTariffThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTariffThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted tariff threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((retries.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): WeightedTariffThreshold {
        return WeightedTariffThreshold(
            code = code,
            label = label,
            retries = retries,
            startsAt = startsAt,
            approved = approved,
            floorValue = floorValue,
            archived = archived,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted tariff threshold in status planned. */
fun newWeightedTariffThreshold(code: String, label: String): WeightedTariffThreshold {
    return WeightedTariffThreshold(
        code = code,
        label = label,
        retries = 0,
        startsAt = 0,
        approved = false,
        floorValue = 0.0,
        archived = false,
        pallets = 0,
        status = WeightedTariffThresholdStatus.PLANNED,
    )
}
