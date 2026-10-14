package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.text.normalize

// The global fuel burn agreement records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a global fuel burn agreement is in its life cycle. */
enum class GlobalFuelBurnAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global fuel burn agreement and its current status. */
data class GlobalFuelBurnAgreement(
    var code: String,
    var label: String,
    var currency: String,
    var vehicleId: String,
    var attempts: Int,
    var status: GlobalFuelBurnAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelBurnAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelBurnAgreementStatus): Boolean {
        when (status) {
            GlobalFuelBurnAgreementStatus.NEW -> {
                when (next) {
                    GlobalFuelBurnAgreementStatus.DONE, GlobalFuelBurnAgreementStatus.FAILED -> return true
                }
                return false
            }
            GlobalFuelBurnAgreementStatus.QUEUED -> {
                when (next) {
                    GlobalFuelBurnAgreementStatus.DONE, GlobalFuelBurnAgreementStatus.FAILED -> return true
                }
                return false
            }
            GlobalFuelBurnAgreementStatus.RUNNING -> return next == GlobalFuelBurnAgreementStatus.DONE || next == GlobalFuelBurnAgreementStatus.FAILED
            GlobalFuelBurnAgreementStatus.DONE -> return next == GlobalFuelBurnAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalFuelBurnAgreementStatus) {
        if (!canMoveTo(next)) {
            error("global fuel burn agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): GlobalFuelBurnAgreement {
        return GlobalFuelBurnAgreement(
            code = code,
            label = label,
            currency = currency,
            vehicleId = vehicleId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global fuel burn agreement in status new. */
fun newGlobalFuelBurnAgreement(code: String, label: String): GlobalFuelBurnAgreement {
    return GlobalFuelBurnAgreement(
        code = code,
        label = label,
        currency = "",
        vehicleId = "",
        attempts = 0,
        status = GlobalFuelBurnAgreementStatus.NEW,
    )
}
