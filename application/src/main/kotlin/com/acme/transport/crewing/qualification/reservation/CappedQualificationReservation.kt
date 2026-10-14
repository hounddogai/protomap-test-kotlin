package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped qualification reservation records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a capped qualification reservation is in its life cycle. */
enum class CappedQualificationReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped qualification reservation. Its code is unique within a tenant. */
data class CappedQualificationReservation(
    var code: String,
    var title: String,
    var amount: Double,
    var rate: Double,
    var batchSize: Int,
    var retries: Int,
    var timezone: String,
    var country: String,
    var status: CappedQualificationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedQualificationReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedQualificationReservationStatus): Boolean {
        when (status) {
            CappedQualificationReservationStatus.CREATED -> return next == CappedQualificationReservationStatus.PUBLISHED || next == CappedQualificationReservationStatus.RETIRED
            CappedQualificationReservationStatus.VALIDATED -> {
                when (next) {
                    CappedQualificationReservationStatus.RETIRED -> return true
                }
                return false
            }
            CappedQualificationReservationStatus.PUBLISHED -> {
                when (next) {
                    CappedQualificationReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedQualificationReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped qualification reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped qualification reservation"} $code"

    fun effectiveValue(): Double = round((rate - retries.toDouble()) * 100) / 100

    fun snapshot(): CappedQualificationReservation {
        return CappedQualificationReservation(
            code = code,
            title = title,
            amount = amount,
            rate = rate,
            batchSize = batchSize,
            retries = retries,
            timezone = timezone,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped qualification reservation in status created. */
fun newCappedQualificationReservation(code: String, title: String): CappedQualificationReservation {
    return CappedQualificationReservation(
        code = code,
        title = title,
        amount = 0.0,
        rate = 0.0,
        batchSize = 0,
        retries = 0,
        timezone = "",
        country = "",
        status = CappedQualificationReservationStatus.CREATED,
    )
}
