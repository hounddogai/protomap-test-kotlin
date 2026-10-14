package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold agreement is in its life cycle. */
enum class HoldAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a hold agreement and its current status. */
data class HoldAgreement(
    var code: String,
    var label: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var minimum: Double,
    var depot: String,
    var priority: Int,
    var contactEmail: String,
    var pinned: Boolean,
    var attempts: Int,
    var approved: Boolean,
    var status: HoldAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldAgreementStatus): Boolean {
        when (status) {
            HoldAgreementStatus.IDLE -> return next == HoldAgreementStatus.STOPPED
            HoldAgreementStatus.WARMING -> {
                when (next) {
                    HoldAgreementStatus.STOPPED -> return true
                }
                return false
            }
            HoldAgreementStatus.READY -> return next == HoldAgreementStatus.STOPPED
            HoldAgreementStatus.DRAINING -> return next == HoldAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HoldAgreementStatus) {
        if (!canMoveTo(next)) {
            error("hold agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"hold agreement"} $code"

    fun adjustedExposure(): Double = round(fuelLiters * priority.toDouble() * 100) / 100

    fun snapshot(): HoldAgreement {
        return HoldAgreement(
            code = code,
            label = label,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            minimum = minimum,
            depot = depot,
            priority = priority,
            contactEmail = contactEmail,
            pinned = pinned,
            attempts = attempts,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a hold agreement in status idle. */
fun newHoldAgreement(code: String, label: String): HoldAgreement {
    return HoldAgreement(
        code = code,
        label = label,
        graceDays = 0,
        fuelLiters = 0.0,
        minimum = 0.0,
        depot = "",
        priority = 0,
        contactEmail = "",
        pinned = false,
        attempts = 0,
        approved = false,
        status = HoldAgreementStatus.IDLE,
    )
}
