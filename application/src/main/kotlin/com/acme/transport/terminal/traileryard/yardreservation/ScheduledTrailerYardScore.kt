package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled trailer yard score is in its life cycle. */
enum class ScheduledTrailerYardScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled trailer yard score. Its code is unique within a tenant. */
data class ScheduledTrailerYardScore(
    var code: String,
    var name: String,
    var phone: String,
    var timezone: String,
    var country: String,
    var windowDays: Int,
    var vehicleId: String,
    var amount: Double,
    var status: ScheduledTrailerYardScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerYardScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerYardScoreStatus): Boolean {
        when (status) {
            ScheduledTrailerYardScoreStatus.PROPOSED -> return next == ScheduledTrailerYardScoreStatus.APPROVED || next == ScheduledTrailerYardScoreStatus.WITHDRAWN
            ScheduledTrailerYardScoreStatus.APPROVED -> return next == ScheduledTrailerYardScoreStatus.REJECTED || next == ScheduledTrailerYardScoreStatus.WITHDRAWN
            ScheduledTrailerYardScoreStatus.REJECTED -> return next == ScheduledTrailerYardScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerYardScoreStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer yard score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((windowDays.toDouble() - amount) * 100) / 100

    fun snapshot(): ScheduledTrailerYardScore {
        return ScheduledTrailerYardScore(
            code = code,
            name = name,
            phone = phone,
            timezone = timezone,
            country = country,
            windowDays = windowDays,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled trailer yard score in status proposed. */
fun newScheduledTrailerYardScore(code: String, name: String): ScheduledTrailerYardScore {
    return ScheduledTrailerYardScore(
        code = code,
        name = name,
        phone = "",
        timezone = "",
        country = "",
        windowDays = 0,
        vehicleId = "",
        amount = 0.0,
        status = ScheduledTrailerYardScoreStatus.PROPOSED,
    )
}
