package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster agreement is in its life cycle. */
enum class StrictRosterAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict roster agreement and its current status. */
data class StrictRosterAgreement(
    var code: String,
    var title: String,
    var startsAt: Int,
    var discount: Double,
    var version: Int,
    var currency: String,
    var contactEmail: String,
    var maximum: Double,
    var vehicleId: String,
    var segment: String,
    var status: StrictRosterAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterAgreementStatus): Boolean {
        when (status) {
            StrictRosterAgreementStatus.PLANNED -> return next == StrictRosterAgreementStatus.SCHEDULED || next == StrictRosterAgreementStatus.IN_PROGRESS
            StrictRosterAgreementStatus.SCHEDULED -> return next == StrictRosterAgreementStatus.CANCELLED
            StrictRosterAgreementStatus.IN_PROGRESS -> return next == StrictRosterAgreementStatus.COMPLETED
            StrictRosterAgreementStatus.COMPLETED -> return next == StrictRosterAgreementStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictRosterAgreementStatus) {
        if (!canMoveTo(next)) {
            error("strict roster agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"strict roster agreement"} $code"

    fun netTotal(): Double = round((startsAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): StrictRosterAgreement {
        return StrictRosterAgreement(
            code = code,
            title = title,
            startsAt = startsAt,
            discount = discount,
            version = version,
            currency = currency,
            contactEmail = contactEmail,
            maximum = maximum,
            vehicleId = vehicleId,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict roster agreement in status planned. */
fun newStrictRosterAgreement(code: String, title: String): StrictRosterAgreement {
    return StrictRosterAgreement(
        code = code,
        title = title,
        startsAt = 0,
        discount = 0.0,
        version = 0,
        currency = "",
        contactEmail = "",
        maximum = 0.0,
        vehicleId = "",
        segment = "",
        status = StrictRosterAgreementStatus.PLANNED,
    )
}
