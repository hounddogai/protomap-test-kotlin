package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual rate card cycle is in its life cycle. */
enum class ManualRateCardCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual rate card cycle as the rate card team keeps it. */
data class ManualRateCardCycle(
    var code: String,
    var label: String,
    var batchSize: Int,
    var startsAt: Int,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var odometer: Int,
    var floorValue: Double,
    var depot: String,
    var timezone: String,
    var rate: Double,
    var status: ManualRateCardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRateCardCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRateCardCycleStatus): Boolean {
        when (status) {
            ManualRateCardCycleStatus.REQUESTED -> {
                when (next) {
                    ManualRateCardCycleStatus.REVOKED, ManualRateCardCycleStatus.EXPIRED -> return true
                }
                return false
            }
            ManualRateCardCycleStatus.GRANTED -> return next == ManualRateCardCycleStatus.REVOKED
            ManualRateCardCycleStatus.REVOKED -> {
                when (next) {
                    ManualRateCardCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRateCardCycleStatus) {
        if (!canMoveTo(next)) {
            error("manual rate card cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((floorValue - odometer.toDouble()) * 100) / 100

    fun snapshot(): ManualRateCardCycle {
        return ManualRateCardCycle(
            code = code,
            label = label,
            batchSize = batchSize,
            startsAt = startsAt,
            attributes = attributes,
            limit = limit,
            odometer = odometer,
            floorValue = floorValue,
            depot = depot,
            timezone = timezone,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual rate card cycle in status requested. */
fun newManualRateCardCycle(code: String, label: String): ManualRateCardCycle {
    return ManualRateCardCycle(
        code = code,
        label = label,
        batchSize = 0,
        startsAt = 0,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        odometer = 0,
        floorValue = 0.0,
        depot = "",
        timezone = "",
        rate = 0.0,
        status = ManualRateCardCycleStatus.REQUESTED,
    )
}
