package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.text.normalize

// The legacy locker agreement records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a legacy locker agreement is in its life cycle. */
enum class LegacyLockerAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy locker agreement as the locker team keeps it. */
data class LegacyLockerAgreement(
    var code: String,
    var title: String,
    var pallets: Int,
    var vehicleId: String,
    var description: String,
    var label: String,
    var currency: String,
    var routeCode: String,
    var status: LegacyLockerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLockerAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLockerAgreementStatus): Boolean {
        when (status) {
            LegacyLockerAgreementStatus.OPEN -> return next == LegacyLockerAgreementStatus.RESOLVED || next == LegacyLockerAgreementStatus.ARCHIVED
            LegacyLockerAgreementStatus.IN_REVIEW -> {
                when (next) {
                    LegacyLockerAgreementStatus.RESOLVED, LegacyLockerAgreementStatus.REOPENED -> return true
                }
                return false
            }
            LegacyLockerAgreementStatus.RESOLVED -> return next == LegacyLockerAgreementStatus.REOPENED || next == LegacyLockerAgreementStatus.ARCHIVED
            LegacyLockerAgreementStatus.REOPENED -> {
                when (next) {
                    LegacyLockerAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLockerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("legacy locker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): LegacyLockerAgreement {
        return LegacyLockerAgreement(
            code = code,
            title = title,
            pallets = pallets,
            vehicleId = vehicleId,
            description = description,
            label = label,
            currency = currency,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy locker agreement in status open. */
fun newLegacyLockerAgreement(code: String, title: String): LegacyLockerAgreement {
    return LegacyLockerAgreement(
        code = code,
        title = title,
        pallets = 0,
        vehicleId = "",
        description = "",
        label = "",
        currency = "",
        routeCode = "",
        status = LegacyLockerAgreementStatus.OPEN,
    )
}
