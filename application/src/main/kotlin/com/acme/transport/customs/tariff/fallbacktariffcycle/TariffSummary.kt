package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff summary is in its life cycle. */
enum class TariffSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tariff summary, with what the planners need to act on it. */
data class TariffSummary(
    var code: String,
    var name: String,
    var shiftCode: String,
    var endsAt: Int,
    var attempts: Int,
    var updatedAt: Int,
    var lane: String,
    var status: TariffSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffSummaryStatus): Boolean {
        when (status) {
            TariffSummaryStatus.DRAFT -> {
                when (next) {
                    TariffSummaryStatus.PENDING, TariffSummaryStatus.ACTIVE -> return true
                }
                return false
            }
            TariffSummaryStatus.PENDING -> return next == TariffSummaryStatus.ACTIVE || next == TariffSummaryStatus.CLOSED
            TariffSummaryStatus.ACTIVE -> return next == TariffSummaryStatus.SUSPENDED || next == TariffSummaryStatus.CLOSED
            TariffSummaryStatus.SUSPENDED -> return next == TariffSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TariffSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tariff summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((attempts.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TariffSummary {
        return TariffSummary(
            code = code,
            name = name,
            shiftCode = shiftCode,
            endsAt = endsAt,
            attempts = attempts,
            updatedAt = updatedAt,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tariff summary in status draft. */
fun newTariffSummary(code: String, name: String): TariffSummary {
    return TariffSummary(
        code = code,
        name = name,
        shiftCode = "",
        endsAt = 0,
        attempts = 0,
        updatedAt = 0,
        lane = "",
        status = TariffSummaryStatus.DRAFT,
    )
}
