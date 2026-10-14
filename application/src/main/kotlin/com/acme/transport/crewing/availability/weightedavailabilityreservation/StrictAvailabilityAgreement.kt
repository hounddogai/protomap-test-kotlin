package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.normalize

/** Where a strict availability agreement is in its life cycle. */
enum class StrictAvailabilityAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict availability agreement as the availability team keeps it. */
data class StrictAvailabilityAgreement(
    var code: String,
    var name: String,
    var region: String,
    var segment: String,
    var dock: String,
    var checksum: String,
    var amount: Double,
    var status: StrictAvailabilityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAvailabilityAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAvailabilityAgreementStatus): Boolean {
        when (status) {
            StrictAvailabilityAgreementStatus.NEW -> {
                when (next) {
                    StrictAvailabilityAgreementStatus.FAILED -> return true
                }
                return false
            }
            StrictAvailabilityAgreementStatus.QUEUED -> return next == StrictAvailabilityAgreementStatus.FAILED
            StrictAvailabilityAgreementStatus.RUNNING -> {
                when (next) {
                    StrictAvailabilityAgreementStatus.FAILED -> return true
                }
                return false
            }
            StrictAvailabilityAgreementStatus.DONE -> return next == StrictAvailabilityAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictAvailabilityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("strict availability agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): StrictAvailabilityAgreement {
        return StrictAvailabilityAgreement(
            code = code,
            name = name,
            region = region,
            segment = segment,
            dock = dock,
            checksum = checksum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict availability agreement in status new. */
fun newStrictAvailabilityAgreement(code: String, name: String): StrictAvailabilityAgreement {
    return StrictAvailabilityAgreement(
        code = code,
        name = name,
        region = "",
        segment = "",
        dock = "",
        checksum = "",
        amount = 0.0,
        status = StrictAvailabilityAgreementStatus.NEW,
    )
}
