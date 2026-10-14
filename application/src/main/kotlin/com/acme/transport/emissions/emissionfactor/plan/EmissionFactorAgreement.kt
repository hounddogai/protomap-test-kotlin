package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor agreement is in its life cycle. */
enum class EmissionFactorAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A emission factor agreement as the emission factor team keeps it. */
data class EmissionFactorAgreement(
    var code: String,
    var title: String,
    var threshold: Double,
    var windowDays: Int,
    var quantity: Int,
    var ownerId: String,
    var status: EmissionFactorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorAgreementStatus): Boolean {
        when (status) {
            EmissionFactorAgreementStatus.PLANNED -> {
                when (next) {
                    EmissionFactorAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            EmissionFactorAgreementStatus.SCHEDULED -> {
                when (next) {
                    EmissionFactorAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            EmissionFactorAgreementStatus.IN_PROGRESS -> return next == EmissionFactorAgreementStatus.COMPLETED || next == EmissionFactorAgreementStatus.CANCELLED
            EmissionFactorAgreementStatus.COMPLETED -> {
                when (next) {
                    EmissionFactorAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("emission factor agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor agreement"} $code"

    fun netVolume(): Double = round((windowDays.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorAgreement {
        return EmissionFactorAgreement(
            code = code,
            title = title,
            threshold = threshold,
            windowDays = windowDays,
            quantity = quantity,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a emission factor agreement in status planned. */
fun newEmissionFactorAgreement(code: String, title: String): EmissionFactorAgreement {
    return EmissionFactorAgreement(
        code = code,
        title = title,
        threshold = 0.0,
        windowDays = 0,
        quantity = 0,
        ownerId = "",
        status = EmissionFactorAgreementStatus.PLANNED,
    )
}
