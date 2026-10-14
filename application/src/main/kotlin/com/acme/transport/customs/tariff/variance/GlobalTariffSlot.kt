package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tariff slot is in its life cycle. */
enum class GlobalTariffSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global tariff slot as the tariff team keeps it. */
data class GlobalTariffSlot(
    var code: String,
    var name: String,
    var externalRef: String,
    var depot: String,
    var margin: Double,
    var reviewer: String,
    var attempts: Int,
    var odometer: Int,
    var batchSize: Int,
    var status: GlobalTariffSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTariffSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTariffSlotStatus): Boolean {
        when (status) {
            GlobalTariffSlotStatus.OPEN -> return next == GlobalTariffSlotStatus.ARCHIVED
            GlobalTariffSlotStatus.IN_REVIEW -> {
                when (next) {
                    GlobalTariffSlotStatus.RESOLVED, GlobalTariffSlotStatus.REOPENED -> return true
                }
                return false
            }
            GlobalTariffSlotStatus.RESOLVED -> {
                when (next) {
                    GlobalTariffSlotStatus.REOPENED, GlobalTariffSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalTariffSlotStatus.REOPENED -> return next == GlobalTariffSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalTariffSlotStatus) {
        if (!canMoveTo(next)) {
            error("global tariff slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round(odometer.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): GlobalTariffSlot {
        return GlobalTariffSlot(
            code = code,
            name = name,
            externalRef = externalRef,
            depot = depot,
            margin = margin,
            reviewer = reviewer,
            attempts = attempts,
            odometer = odometer,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global tariff slot in status open. */
fun newGlobalTariffSlot(code: String, name: String): GlobalTariffSlot {
    return GlobalTariffSlot(
        code = code,
        name = name,
        externalRef = "",
        depot = "",
        margin = 0.0,
        reviewer = "",
        attempts = 0,
        odometer = 0,
        batchSize = 0,
        status = GlobalTariffSlotStatus.OPEN,
    )
}
