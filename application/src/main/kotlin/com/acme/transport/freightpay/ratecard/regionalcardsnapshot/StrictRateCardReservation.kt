package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rate card reservation is in its life cycle. */
enum class StrictRateCardReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict rate card reservation and its current status. */
data class StrictRateCardReservation(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var locale: String,
    var priority: Int,
    var createdAt: Int,
    var retries: Int,
    var revision: Int,
    var capacity: Int,
    var archived: Boolean,
    var phone: String,
    var status: StrictRateCardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardReservationStatus): Boolean {
        when (status) {
            StrictRateCardReservationStatus.PROPOSED -> {
                when (next) {
                    StrictRateCardReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictRateCardReservationStatus.APPROVED -> {
                when (next) {
                    StrictRateCardReservationStatus.REJECTED, StrictRateCardReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictRateCardReservationStatus.REJECTED -> return next == StrictRateCardReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictRateCardReservationStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round((capacity.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): StrictRateCardReservation {
        return StrictRateCardReservation(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            locale = locale,
            priority = priority,
            createdAt = createdAt,
            retries = retries,
            revision = revision,
            capacity = capacity,
            archived = archived,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict rate card reservation in status proposed. */
fun newStrictRateCardReservation(code: String, label: String): StrictRateCardReservation {
    return StrictRateCardReservation(
        code = code,
        label = label,
        etaMinutes = 0,
        locale = "",
        priority = 0,
        createdAt = 0,
        retries = 0,
        revision = 0,
        capacity = 0,
        archived = false,
        phone = "",
        status = StrictRateCardReservationStatus.PROPOSED,
    )
}
