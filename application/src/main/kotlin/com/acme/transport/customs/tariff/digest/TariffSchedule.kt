package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff schedule is in its life cycle. */
enum class TariffScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tariff schedule, with what the planners need to act on it. */
data class TariffSchedule(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var country: String,
    var endsAt: Int,
    var discount: Double,
    var sequence: Int,
    var startsAt: Int,
    var ceiling: Double,
    var status: TariffScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffScheduleStatus): Boolean {
        when (status) {
            TariffScheduleStatus.DRAFT -> return next == TariffScheduleStatus.PENDING
            TariffScheduleStatus.PENDING -> return next == TariffScheduleStatus.ACTIVE || next == TariffScheduleStatus.SUSPENDED
            TariffScheduleStatus.ACTIVE -> return next == TariffScheduleStatus.SUSPENDED || next == TariffScheduleStatus.CLOSED
            TariffScheduleStatus.SUSPENDED -> return next == TariffScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TariffScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tariff schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tariff schedule"} $code"

    fun adjustedLoad(): Double = round((endsAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): TariffSchedule {
        return TariffSchedule(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            country = country,
            endsAt = endsAt,
            discount = discount,
            sequence = sequence,
            startsAt = startsAt,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tariff schedule in status draft. */
fun newTariffSchedule(code: String, title: String): TariffSchedule {
    return TariffSchedule(
        code = code,
        title = title,
        etaMinutes = 0,
        country = "",
        endsAt = 0,
        discount = 0.0,
        sequence = 0,
        startsAt = 0,
        ceiling = 0.0,
        status = TariffScheduleStatus.DRAFT,
    )
}
