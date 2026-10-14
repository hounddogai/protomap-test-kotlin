package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection agreement is in its life cycle. */
enum class InspectionAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection agreement. Its code is unique within a tenant. */
data class InspectionAgreement(
    var code: String,
    var label: String,
    var description: String,
    var threshold: Double,
    var rate: Double,
    var pallets: Int,
    var status: InspectionAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAgreementStatus): Boolean {
        when (status) {
            InspectionAgreementStatus.PLANNED -> {
                when (next) {
                    InspectionAgreementStatus.IN_PROGRESS, InspectionAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            InspectionAgreementStatus.SCHEDULED -> return next == InspectionAgreementStatus.IN_PROGRESS || next == InspectionAgreementStatus.CANCELLED
            InspectionAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionAgreementStatus.COMPLETED, InspectionAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionAgreementStatus.COMPLETED -> return next == InspectionAgreementStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionAgreementStatus) {
        if (!canMoveTo(next)) {
            error("inspection agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((threshold + pallets.toDouble()) * 100) / 100

    fun snapshot(): InspectionAgreement {
        return InspectionAgreement(
            code = code,
            label = label,
            description = description,
            threshold = threshold,
            rate = rate,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection agreement in status planned. */
fun newInspectionAgreement(code: String, label: String): InspectionAgreement {
    return InspectionAgreement(
        code = code,
        label = label,
        description = "",
        threshold = 0.0,
        rate = 0.0,
        pallets = 0,
        status = InspectionAgreementStatus.PLANNED,
    )
}
