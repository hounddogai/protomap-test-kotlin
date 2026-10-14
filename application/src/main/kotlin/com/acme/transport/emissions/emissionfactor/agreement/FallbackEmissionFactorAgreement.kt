package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback emission factor agreement records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a fallback emission factor agreement is in its life cycle. */
enum class FallbackEmissionFactorAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback emission factor agreement, with what the planners need to act on it. */
data class FallbackEmissionFactorAgreement(
    var code: String,
    var title: String,
    var contactEmail: String,
    var lane: String,
    var vehicleId: String,
    var phone: String,
    var minimum: Double,
    var label: String,
    var pallets: Int,
    var active: Boolean,
    var description: String,
    var status: FallbackEmissionFactorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEmissionFactorAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEmissionFactorAgreementStatus): Boolean {
        when (status) {
            FallbackEmissionFactorAgreementStatus.PROPOSED -> return next == FallbackEmissionFactorAgreementStatus.REJECTED || next == FallbackEmissionFactorAgreementStatus.WITHDRAWN
            FallbackEmissionFactorAgreementStatus.APPROVED -> {
                when (next) {
                    FallbackEmissionFactorAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackEmissionFactorAgreementStatus.REJECTED -> {
                when (next) {
                    FallbackEmissionFactorAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEmissionFactorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("fallback emission factor agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedExposure(): Double = round((pallets.toDouble() - minimum) * 100) / 100

    fun snapshot(): FallbackEmissionFactorAgreement {
        return FallbackEmissionFactorAgreement(
            code = code,
            title = title,
            contactEmail = contactEmail,
            lane = lane,
            vehicleId = vehicleId,
            phone = phone,
            minimum = minimum,
            label = label,
            pallets = pallets,
            active = active,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback emission factor agreement in status proposed. */
fun newFallbackEmissionFactorAgreement(code: String, title: String): FallbackEmissionFactorAgreement {
    return FallbackEmissionFactorAgreement(
        code = code,
        title = title,
        contactEmail = "",
        lane = "",
        vehicleId = "",
        phone = "",
        minimum = 0.0,
        label = "",
        pallets = 0,
        active = false,
        description = "",
        status = FallbackEmissionFactorAgreementStatus.PROPOSED,
    )
}
