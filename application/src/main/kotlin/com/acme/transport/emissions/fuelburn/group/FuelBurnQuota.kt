package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn quota is in its life cycle. */
enum class FuelBurnQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fuel burn quota and its current status. */
data class FuelBurnQuota(
    var code: String,
    var title: String,
    var carrier: String,
    var active: Boolean,
    var capacity: Int,
    var attempts: Int,
    var startsAt: Int,
    var floorValue: Double,
    var status: FuelBurnQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnQuotaStatus): Boolean {
        when (status) {
            FuelBurnQuotaStatus.NEW -> {
                when (next) {
                    FuelBurnQuotaStatus.QUEUED -> return true
                }
                return false
            }
            FuelBurnQuotaStatus.QUEUED -> {
                when (next) {
                    FuelBurnQuotaStatus.RUNNING, FuelBurnQuotaStatus.DONE -> return true
                }
                return false
            }
            FuelBurnQuotaStatus.RUNNING -> return next == FuelBurnQuotaStatus.DONE || next == FuelBurnQuotaStatus.FAILED
            FuelBurnQuotaStatus.DONE -> return next == FuelBurnQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FuelBurnQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round((startsAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnQuota {
        return FuelBurnQuota(
            code = code,
            title = title,
            carrier = carrier,
            active = active,
            capacity = capacity,
            attempts = attempts,
            startsAt = startsAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn quota in status new. */
fun newFuelBurnQuota(code: String, title: String): FuelBurnQuota {
    return FuelBurnQuota(
        code = code,
        title = title,
        carrier = "",
        active = false,
        capacity = 0,
        attempts = 0,
        startsAt = 0,
        floorValue = 0.0,
        status = FuelBurnQuotaStatus.NEW,
    )
}
