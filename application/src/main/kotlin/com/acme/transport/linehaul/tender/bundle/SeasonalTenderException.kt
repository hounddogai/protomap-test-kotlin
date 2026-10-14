package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal tender exception is in its life cycle. */
enum class SeasonalTenderExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal tender exception and its current status. */
data class SeasonalTenderException(
    var code: String,
    var title: String,
    var endsAt: Int,
    var ratio: Double,
    var notes: String,
    var channel: String,
    var vehicleId: String,
    var dock: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var minimum: Double,
    var status: SeasonalTenderExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTenderExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTenderExceptionStatus): Boolean {
        when (status) {
            SeasonalTenderExceptionStatus.IDLE -> {
                when (next) {
                    SeasonalTenderExceptionStatus.DRAINING, SeasonalTenderExceptionStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalTenderExceptionStatus.WARMING -> {
                when (next) {
                    SeasonalTenderExceptionStatus.READY, SeasonalTenderExceptionStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalTenderExceptionStatus.READY -> return next == SeasonalTenderExceptionStatus.STOPPED
            SeasonalTenderExceptionStatus.DRAINING -> {
                when (next) {
                    SeasonalTenderExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTenderExceptionStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tender exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round(endsAt.toDouble() * ratio * 100) / 100

    fun snapshot(): SeasonalTenderException {
        return SeasonalTenderException(
            code = code,
            title = title,
            endsAt = endsAt,
            ratio = ratio,
            notes = notes,
            channel = channel,
            vehicleId = vehicleId,
            dock = dock,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal tender exception in status idle. */
fun newSeasonalTenderException(code: String, title: String): SeasonalTenderException {
    return SeasonalTenderException(
        code = code,
        title = title,
        endsAt = 0,
        ratio = 0.0,
        notes = "",
        channel = "",
        vehicleId = "",
        dock = "",
        etaMinutes = 0,
        shiftCode = "",
        minimum = 0.0,
        status = SeasonalTenderExceptionStatus.IDLE,
    )
}
