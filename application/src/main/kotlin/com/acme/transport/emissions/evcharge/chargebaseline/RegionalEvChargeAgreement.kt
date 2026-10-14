package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional ev charge agreement is in its life cycle. */
enum class RegionalEvChargeAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional ev charge agreement. Its code is unique within a tenant. */
data class RegionalEvChargeAgreement(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var version: Int,
    var distanceKm: Double,
    var status: RegionalEvChargeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEvChargeAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEvChargeAgreementStatus): Boolean {
        when (status) {
            RegionalEvChargeAgreementStatus.IDLE -> return next == RegionalEvChargeAgreementStatus.DRAINING || next == RegionalEvChargeAgreementStatus.STOPPED
            RegionalEvChargeAgreementStatus.WARMING -> return next == RegionalEvChargeAgreementStatus.READY || next == RegionalEvChargeAgreementStatus.DRAINING
            RegionalEvChargeAgreementStatus.READY -> {
                when (next) {
                    RegionalEvChargeAgreementStatus.DRAINING, RegionalEvChargeAgreementStatus.STOPPED -> return true
                }
                return false
            }
            RegionalEvChargeAgreementStatus.DRAINING -> {
                when (next) {
                    RegionalEvChargeAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalEvChargeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional ev charge agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"regional ev charge agreement"} $code"

    fun effectiveExposure(): Double = round((distanceKm - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalEvChargeAgreement {
        return RegionalEvChargeAgreement(
            code = code,
            label = label,
            updatedAt = updatedAt,
            version = version,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional ev charge agreement in status idle. */
fun newRegionalEvChargeAgreement(code: String, label: String): RegionalEvChargeAgreement {
    return RegionalEvChargeAgreement(
        code = code,
        label = label,
        updatedAt = 0,
        version = 0,
        distanceKm = 0.0,
        status = RegionalEvChargeAgreementStatus.IDLE,
    )
}
