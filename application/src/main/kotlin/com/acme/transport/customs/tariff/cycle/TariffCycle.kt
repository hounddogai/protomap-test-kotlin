package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff cycle records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff cycle is in its life cycle. */
enum class TariffCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tariff cycle and its current status. */
data class TariffCycle(
    var code: String,
    var label: String,
    var trailerId: String,
    var description: String,
    var category: String,
    var expiresAt: Int,
    var depot: String,
    var pallets: Int,
    var status: TariffCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffCycleStatus): Boolean {
        when (status) {
            TariffCycleStatus.REQUESTED -> {
                when (next) {
                    TariffCycleStatus.GRANTED, TariffCycleStatus.EXPIRED -> return true
                }
                return false
            }
            TariffCycleStatus.GRANTED -> return next == TariffCycleStatus.EXPIRED
            TariffCycleStatus.REVOKED -> return next == TariffCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TariffCycleStatus) {
        if (!canMoveTo(next)) {
            error("tariff cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): TariffCycle {
        return TariffCycle(
            code = code,
            label = label,
            trailerId = trailerId,
            description = description,
            category = category,
            expiresAt = expiresAt,
            depot = depot,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tariff cycle in status requested. */
fun newTariffCycle(code: String, label: String): TariffCycle {
    return TariffCycle(
        code = code,
        label = label,
        trailerId = "",
        description = "",
        category = "",
        expiresAt = 0,
        depot = "",
        pallets = 0,
        status = TariffCycleStatus.REQUESTED,
    )
}
