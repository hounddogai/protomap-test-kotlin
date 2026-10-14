package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.normalize

// The regional service area agreement records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a regional service area agreement is in its life cycle. */
enum class RegionalServiceAreaAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional service area agreement and its current status. */
data class RegionalServiceAreaAgreement(
    var code: String,
    var name: String,
    var odometer: Int,
    var driverId: String,
    var shiftCode: String,
    var status: RegionalServiceAreaAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaAgreementStatus): Boolean {
        when (status) {
            RegionalServiceAreaAgreementStatus.NEW -> {
                when (next) {
                    RegionalServiceAreaAgreementStatus.RUNNING, RegionalServiceAreaAgreementStatus.DONE -> return true
                }
                return false
            }
            RegionalServiceAreaAgreementStatus.QUEUED -> {
                when (next) {
                    RegionalServiceAreaAgreementStatus.RUNNING, RegionalServiceAreaAgreementStatus.FAILED -> return true
                }
                return false
            }
            RegionalServiceAreaAgreementStatus.RUNNING -> {
                when (next) {
                    RegionalServiceAreaAgreementStatus.FAILED -> return true
                }
                return false
            }
            RegionalServiceAreaAgreementStatus.DONE -> return next == RegionalServiceAreaAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional service area agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional service area agreement"} $code"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): RegionalServiceAreaAgreement {
        return RegionalServiceAreaAgreement(
            code = code,
            name = name,
            odometer = odometer,
            driverId = driverId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional service area agreement in status new. */
fun newRegionalServiceAreaAgreement(code: String, name: String): RegionalServiceAreaAgreement {
    return RegionalServiceAreaAgreement(
        code = code,
        name = name,
        odometer = 0,
        driverId = "",
        shiftCode = "",
        status = RegionalServiceAreaAgreementStatus.NEW,
    )
}
