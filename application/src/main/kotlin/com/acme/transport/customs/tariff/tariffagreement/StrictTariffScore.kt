package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.text.normalize

/** Where a strict tariff score is in its life cycle. */
enum class StrictTariffScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict tariff score as the tariff team keeps it. */
data class StrictTariffScore(
    var code: String,
    var title: String,
    var channel: String,
    var approved: Boolean,
    var active: Boolean,
    var label: String,
    var lane: String,
    var notes: String,
    var phone: String,
    var country: String,
    var score: Double,
    var status: StrictTariffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTariffScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTariffScoreStatus): Boolean {
        when (status) {
            StrictTariffScoreStatus.PLANNED -> return next == StrictTariffScoreStatus.COMPLETED || next == StrictTariffScoreStatus.CANCELLED
            StrictTariffScoreStatus.SCHEDULED -> {
                when (next) {
                    StrictTariffScoreStatus.COMPLETED -> return true
                }
                return false
            }
            StrictTariffScoreStatus.IN_PROGRESS -> {
                when (next) {
                    StrictTariffScoreStatus.COMPLETED -> return true
                }
                return false
            }
            StrictTariffScoreStatus.COMPLETED -> return next == StrictTariffScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictTariffScoreStatus) {
        if (!canMoveTo(next)) {
            error("strict tariff score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun measuredValue(): Double = score

    fun snapshot(): StrictTariffScore {
        return StrictTariffScore(
            code = code,
            title = title,
            channel = channel,
            approved = approved,
            active = active,
            label = label,
            lane = lane,
            notes = notes,
            phone = phone,
            country = country,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict tariff score in status planned. */
fun newStrictTariffScore(code: String, title: String): StrictTariffScore {
    return StrictTariffScore(
        code = code,
        title = title,
        channel = "",
        approved = false,
        active = false,
        label = "",
        lane = "",
        notes = "",
        phone = "",
        country = "",
        score = 0.0,
        status = StrictTariffScoreStatus.PLANNED,
    )
}
