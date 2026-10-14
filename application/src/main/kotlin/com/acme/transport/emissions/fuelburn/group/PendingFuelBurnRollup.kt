package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending fuel burn rollup is in its life cycle. */
enum class PendingFuelBurnRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending fuel burn rollup. Its code is unique within a tenant. */
data class PendingFuelBurnRollup(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var archived: Boolean,
    var phone: String,
    var country: String,
    var description: String,
    var region: String,
    var weight: Double,
    var ratio: Double,
    var floorValue: Double,
    var status: PendingFuelBurnRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelBurnRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelBurnRollupStatus): Boolean {
        when (status) {
            PendingFuelBurnRollupStatus.PLANNED -> {
                when (next) {
                    PendingFuelBurnRollupStatus.SCHEDULED -> return true
                }
                return false
            }
            PendingFuelBurnRollupStatus.SCHEDULED -> {
                when (next) {
                    PendingFuelBurnRollupStatus.COMPLETED, PendingFuelBurnRollupStatus.CANCELLED -> return true
                }
                return false
            }
            PendingFuelBurnRollupStatus.IN_PROGRESS -> return next == PendingFuelBurnRollupStatus.COMPLETED || next == PendingFuelBurnRollupStatus.CANCELLED
            PendingFuelBurnRollupStatus.COMPLETED -> {
                when (next) {
                    PendingFuelBurnRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFuelBurnRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel burn rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((floorValue + ratio) * 100) / 100

    fun snapshot(): PendingFuelBurnRollup {
        return PendingFuelBurnRollup(
            code = code,
            label = label,
            distanceKm = distanceKm,
            archived = archived,
            phone = phone,
            country = country,
            description = description,
            region = region,
            weight = weight,
            ratio = ratio,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending fuel burn rollup in status planned. */
fun newPendingFuelBurnRollup(code: String, label: String): PendingFuelBurnRollup {
    return PendingFuelBurnRollup(
        code = code,
        label = label,
        distanceKm = 0.0,
        archived = false,
        phone = "",
        country = "",
        description = "",
        region = "",
        weight = 0.0,
        ratio = 0.0,
        floorValue = 0.0,
        status = PendingFuelBurnRollupStatus.PLANNED,
    )
}
