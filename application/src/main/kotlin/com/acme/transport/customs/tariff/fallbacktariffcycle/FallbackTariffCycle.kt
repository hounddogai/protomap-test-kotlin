package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize

// The fallback tariff cycle records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a fallback tariff cycle is in its life cycle. */
enum class FallbackTariffCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback tariff cycle as the tariff team keeps it. */
data class FallbackTariffCycle(
    var code: String,
    var title: String,
    var currency: String,
    var contactEmail: String,
    var timezone: String,
    var approved: Boolean,
    var phone: String,
    var stopCount: Int,
    var source: String,
    var status: FallbackTariffCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTariffCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTariffCycleStatus): Boolean {
        when (status) {
            FallbackTariffCycleStatus.NEW -> {
                when (next) {
                    FallbackTariffCycleStatus.QUEUED, FallbackTariffCycleStatus.FAILED -> return true
                }
                return false
            }
            FallbackTariffCycleStatus.QUEUED -> {
                when (next) {
                    FallbackTariffCycleStatus.RUNNING, FallbackTariffCycleStatus.DONE -> return true
                }
                return false
            }
            FallbackTariffCycleStatus.RUNNING -> return next == FallbackTariffCycleStatus.DONE || next == FallbackTariffCycleStatus.FAILED
            FallbackTariffCycleStatus.DONE -> return next == FallbackTariffCycleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackTariffCycleStatus) {
        if (!canMoveTo(next)) {
            error("fallback tariff cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): FallbackTariffCycle {
        return FallbackTariffCycle(
            code = code,
            title = title,
            currency = currency,
            contactEmail = contactEmail,
            timezone = timezone,
            approved = approved,
            phone = phone,
            stopCount = stopCount,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback tariff cycle in status new. */
fun newFallbackTariffCycle(code: String, title: String): FallbackTariffCycle {
    return FallbackTariffCycle(
        code = code,
        title = title,
        currency = "",
        contactEmail = "",
        timezone = "",
        approved = false,
        phone = "",
        stopCount = 0,
        source = "",
        status = FallbackTariffCycleStatus.NEW,
    )
}
