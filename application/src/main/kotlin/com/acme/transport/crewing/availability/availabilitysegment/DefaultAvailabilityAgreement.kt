package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.text.normalize

/** Where a default availability agreement is in its life cycle. */
enum class DefaultAvailabilityAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default availability agreement. Its code is unique within a tenant. */
data class DefaultAvailabilityAgreement(
    var code: String,
    var label: String,
    var score: Double,
    var pinned: Boolean,
    var ownerId: String,
    var status: DefaultAvailabilityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAvailabilityAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAvailabilityAgreementStatus): Boolean {
        when (status) {
            DefaultAvailabilityAgreementStatus.IDLE -> {
                when (next) {
                    DefaultAvailabilityAgreementStatus.DRAINING, DefaultAvailabilityAgreementStatus.STOPPED -> return true
                }
                return false
            }
            DefaultAvailabilityAgreementStatus.WARMING -> return next == DefaultAvailabilityAgreementStatus.READY || next == DefaultAvailabilityAgreementStatus.STOPPED
            DefaultAvailabilityAgreementStatus.READY -> {
                when (next) {
                    DefaultAvailabilityAgreementStatus.STOPPED -> return true
                }
                return false
            }
            DefaultAvailabilityAgreementStatus.DRAINING -> {
                when (next) {
                    DefaultAvailabilityAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultAvailabilityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default availability agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): DefaultAvailabilityAgreement {
        return DefaultAvailabilityAgreement(
            code = code,
            label = label,
            score = score,
            pinned = pinned,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default availability agreement in status idle. */
fun newDefaultAvailabilityAgreement(code: String, label: String): DefaultAvailabilityAgreement {
    return DefaultAvailabilityAgreement(
        code = code,
        label = label,
        score = 0.0,
        pinned = false,
        ownerId = "",
        status = DefaultAvailabilityAgreementStatus.IDLE,
    )
}
