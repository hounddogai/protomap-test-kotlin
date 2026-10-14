package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize

// The primary inspection agreement records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a primary inspection agreement is in its life cycle. */
enum class PrimaryInspectionAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary inspection agreement as the inspection team keeps it. */
data class PrimaryInspectionAgreement(
    var code: String,
    var label: String,
    var source: String,
    var description: String,
    var startsAt: Int,
    var routeCode: String,
    var status: PrimaryInspectionAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionAgreementStatus): Boolean {
        when (status) {
            PrimaryInspectionAgreementStatus.REQUESTED -> return next == PrimaryInspectionAgreementStatus.GRANTED || next == PrimaryInspectionAgreementStatus.EXPIRED
            PrimaryInspectionAgreementStatus.GRANTED -> return next == PrimaryInspectionAgreementStatus.REVOKED
            PrimaryInspectionAgreementStatus.REVOKED -> return next == PrimaryInspectionAgreementStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionAgreementStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): PrimaryInspectionAgreement {
        return PrimaryInspectionAgreement(
            code = code,
            label = label,
            source = source,
            description = description,
            startsAt = startsAt,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary inspection agreement in status requested. */
fun newPrimaryInspectionAgreement(code: String, label: String): PrimaryInspectionAgreement {
    return PrimaryInspectionAgreement(
        code = code,
        label = label,
        source = "",
        description = "",
        startsAt = 0,
        routeCode = "",
        status = PrimaryInspectionAgreementStatus.REQUESTED,
    )
}
