package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy cutoff reservation is in its life cycle. */
enum class LegacyCutoffReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One legacy cutoff reservation, with what the planners need to act on it. */
data class LegacyCutoffReservation(
    var code: String,
    var label: String,
    var discount: Double,
    var cost: Double,
    var retries: Int,
    var minimum: Double,
    var title: String,
    var revision: Int,
    var summary: String,
    var sequence: Int,
    var status: LegacyCutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCutoffReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCutoffReservationStatus): Boolean {
        when (status) {
            LegacyCutoffReservationStatus.CREATED -> {
                when (next) {
                    LegacyCutoffReservationStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyCutoffReservationStatus.VALIDATED -> {
                when (next) {
                    LegacyCutoffReservationStatus.PUBLISHED, LegacyCutoffReservationStatus.RETIRED -> return true
                }
                return false
            }
            LegacyCutoffReservationStatus.PUBLISHED -> return next == LegacyCutoffReservationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyCutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy cutoff reservation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"legacy cutoff reservation"} $code"

    fun projectedTotal(): Double = round(retries.toDouble() * discount * 100) / 100

    fun snapshot(): LegacyCutoffReservation {
        return LegacyCutoffReservation(
            code = code,
            label = label,
            discount = discount,
            cost = cost,
            retries = retries,
            minimum = minimum,
            title = title,
            revision = revision,
            summary = summary,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy cutoff reservation in status created. */
fun newLegacyCutoffReservation(code: String, label: String): LegacyCutoffReservation {
    return LegacyCutoffReservation(
        code = code,
        label = label,
        discount = 0.0,
        cost = 0.0,
        retries = 0,
        minimum = 0.0,
        title = "",
        revision = 0,
        summary = "",
        sequence = 0,
        status = LegacyCutoffReservationStatus.CREATED,
    )
}
