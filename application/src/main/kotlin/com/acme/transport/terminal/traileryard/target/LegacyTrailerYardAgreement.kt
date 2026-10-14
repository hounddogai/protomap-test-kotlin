package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy trailer yard agreement is in its life cycle. */
enum class LegacyTrailerYardAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy trailer yard agreement. Its code is unique within a tenant. */
data class LegacyTrailerYardAgreement(
    var code: String,
    var title: String,
    var version: Int,
    var currency: String,
    var odometer: Int,
    var depot: String,
    var vehicleId: String,
    var status: LegacyTrailerYardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardAgreementStatus): Boolean {
        when (status) {
            LegacyTrailerYardAgreementStatus.IDLE -> return next == LegacyTrailerYardAgreementStatus.WARMING || next == LegacyTrailerYardAgreementStatus.READY
            LegacyTrailerYardAgreementStatus.WARMING -> {
                when (next) {
                    LegacyTrailerYardAgreementStatus.READY, LegacyTrailerYardAgreementStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTrailerYardAgreementStatus.READY -> {
                when (next) {
                    LegacyTrailerYardAgreementStatus.DRAINING, LegacyTrailerYardAgreementStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTrailerYardAgreementStatus.DRAINING -> return next == LegacyTrailerYardAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard agreement $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"legacy trailer yard agreement"} $code"

    fun adjustedVolume(): Double = round(odometer.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): LegacyTrailerYardAgreement {
        return LegacyTrailerYardAgreement(
            code = code,
            title = title,
            version = version,
            currency = currency,
            odometer = odometer,
            depot = depot,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy trailer yard agreement in status idle. */
fun newLegacyTrailerYardAgreement(code: String, title: String): LegacyTrailerYardAgreement {
    return LegacyTrailerYardAgreement(
        code = code,
        title = title,
        version = 0,
        currency = "",
        odometer = 0,
        depot = "",
        vehicleId = "",
        status = LegacyTrailerYardAgreementStatus.IDLE,
    )
}
