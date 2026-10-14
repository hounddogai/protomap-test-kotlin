package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize

/** Where a seasonal trailer yard reservation is in its life cycle. */
enum class SeasonalTrailerYardReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One seasonal trailer yard reservation, with what the planners need to act on it. */
data class SeasonalTrailerYardReservation(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var createdAt: Int,
    var depot: String,
    var active: Boolean,
    var status: SeasonalTrailerYardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardReservationStatus): Boolean {
        when (status) {
            SeasonalTrailerYardReservationStatus.NEW -> {
                when (next) {
                    SeasonalTrailerYardReservationStatus.RUNNING, SeasonalTrailerYardReservationStatus.DONE -> return true
                }
                return false
            }
            SeasonalTrailerYardReservationStatus.QUEUED -> return next == SeasonalTrailerYardReservationStatus.DONE || next == SeasonalTrailerYardReservationStatus.FAILED
            SeasonalTrailerYardReservationStatus.RUNNING -> {
                when (next) {
                    SeasonalTrailerYardReservationStatus.DONE, SeasonalTrailerYardReservationStatus.FAILED -> return true
                }
                return false
            }
            SeasonalTrailerYardReservationStatus.DONE -> {
                when (next) {
                    SeasonalTrailerYardReservationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardReservationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): SeasonalTrailerYardReservation {
        return SeasonalTrailerYardReservation(
            code = code,
            label = label,
            tags = tags,
            createdAt = createdAt,
            depot = depot,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal trailer yard reservation in status new. */
fun newSeasonalTrailerYardReservation(code: String, label: String): SeasonalTrailerYardReservation {
    return SeasonalTrailerYardReservation(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        createdAt = 0,
        depot = "",
        active = false,
        status = SeasonalTrailerYardReservationStatus.NEW,
    )
}
