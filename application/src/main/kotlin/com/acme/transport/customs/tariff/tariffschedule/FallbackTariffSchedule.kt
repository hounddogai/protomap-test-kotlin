package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback tariff schedule records of customs's tariff work, and the rules, stores, and calculations
// around them.

/** Where a fallback tariff schedule is in its life cycle. */
enum class FallbackTariffScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback tariff schedule, with what the planners need to act on it. */
data class FallbackTariffSchedule(
    var code: String,
    var name: String,
    var score: Double,
    var trailerId: String,
    var visible: Boolean,
    var limit: Int,
    var status: FallbackTariffScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTariffScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTariffScheduleStatus): Boolean {
        when (status) {
            FallbackTariffScheduleStatus.DRAFT -> {
                when (next) {
                    FallbackTariffScheduleStatus.PENDING, FallbackTariffScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackTariffScheduleStatus.PENDING -> {
                when (next) {
                    FallbackTariffScheduleStatus.SUSPENDED, FallbackTariffScheduleStatus.CLOSED -> return true
                }
                return false
            }
            FallbackTariffScheduleStatus.ACTIVE -> {
                when (next) {
                    FallbackTariffScheduleStatus.CLOSED -> return true
                }
                return false
            }
            FallbackTariffScheduleStatus.SUSPENDED -> return next == FallbackTariffScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackTariffScheduleStatus) {
        if (!canMoveTo(next)) {
            error("fallback tariff schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback tariff schedule"} $code"

    fun weightedLoad(): Double = round((limit.toDouble() + score) * 100) / 100

    fun snapshot(): FallbackTariffSchedule {
        return FallbackTariffSchedule(
            code = code,
            name = name,
            score = score,
            trailerId = trailerId,
            visible = visible,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback tariff schedule in status draft. */
fun newFallbackTariffSchedule(code: String, name: String): FallbackTariffSchedule {
    return FallbackTariffSchedule(
        code = code,
        name = name,
        score = 0.0,
        trailerId = "",
        visible = false,
        limit = 0,
        status = FallbackTariffScheduleStatus.DRAFT,
    )
}
