package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff baseline is in its life cycle. */
enum class TariffBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tariff baseline as the tariff team keeps it. */
data class TariffBaseline(
    var code: String,
    var title: String,
    var attempts: Int,
    var maximum: Double,
    var vehicleId: String,
    var score: Double,
    var notes: String,
    var ratio: Double,
    var status: TariffBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffBaselineStatus): Boolean {
        when (status) {
            TariffBaselineStatus.NEW -> {
                when (next) {
                    TariffBaselineStatus.DONE -> return true
                }
                return false
            }
            TariffBaselineStatus.QUEUED -> {
                when (next) {
                    TariffBaselineStatus.RUNNING, TariffBaselineStatus.DONE -> return true
                }
                return false
            }
            TariffBaselineStatus.RUNNING -> {
                when (next) {
                    TariffBaselineStatus.DONE, TariffBaselineStatus.FAILED -> return true
                }
                return false
            }
            TariffBaselineStatus.DONE -> {
                when (next) {
                    TariffBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tariff baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((score - ratio) * 100) / 100

    fun snapshot(): TariffBaseline {
        return TariffBaseline(
            code = code,
            title = title,
            attempts = attempts,
            maximum = maximum,
            vehicleId = vehicleId,
            score = score,
            notes = notes,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff baseline in status new. */
fun newTariffBaseline(code: String, title: String): TariffBaseline {
    return TariffBaseline(
        code = code,
        title = title,
        attempts = 0,
        maximum = 0.0,
        vehicleId = "",
        score = 0.0,
        notes = "",
        ratio = 0.0,
        status = TariffBaselineStatus.NEW,
    )
}
