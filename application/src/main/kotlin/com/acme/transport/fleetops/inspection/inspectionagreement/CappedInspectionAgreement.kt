package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.text.normalize

// The capped inspection agreement records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a capped inspection agreement is in its life cycle. */
enum class CappedInspectionAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped inspection agreement. Its code is unique within a tenant. */
data class CappedInspectionAgreement(
    var code: String,
    var name: String,
    var country: String,
    var odometer: Int,
    var trailerId: String,
    var status: CappedInspectionAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionAgreementStatus): Boolean {
        when (status) {
            CappedInspectionAgreementStatus.PROPOSED -> return next == CappedInspectionAgreementStatus.REJECTED || next == CappedInspectionAgreementStatus.WITHDRAWN
            CappedInspectionAgreementStatus.APPROVED -> {
                when (next) {
                    CappedInspectionAgreementStatus.REJECTED, CappedInspectionAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedInspectionAgreementStatus.REJECTED -> return next == CappedInspectionAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedInspectionAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): CappedInspectionAgreement {
        return CappedInspectionAgreement(
            code = code,
            name = name,
            country = country,
            odometer = odometer,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped inspection agreement in status proposed. */
fun newCappedInspectionAgreement(code: String, name: String): CappedInspectionAgreement {
    return CappedInspectionAgreement(
        code = code,
        name = name,
        country = "",
        odometer = 0,
        trailerId = "",
        status = CappedInspectionAgreementStatus.PROPOSED,
    )
}
