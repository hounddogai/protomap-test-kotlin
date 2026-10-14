package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.normalize

/** Where a violation agreement is in its life cycle. */
enum class ViolationAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A violation agreement as the violation team keeps it. */
data class ViolationAgreement(
    var code: String,
    var title: String,
    var shiftCode: String,
    var trailerId: String,
    var driverId: String,
    var active: Boolean,
    var amount: Double,
    var status: ViolationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationAgreementStatus): Boolean {
        when (status) {
            ViolationAgreementStatus.OPEN -> {
                when (next) {
                    ViolationAgreementStatus.IN_REVIEW -> return true
                }
                return false
            }
            ViolationAgreementStatus.IN_REVIEW -> {
                when (next) {
                    ViolationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ViolationAgreementStatus.RESOLVED -> {
                when (next) {
                    ViolationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            ViolationAgreementStatus.REOPENED -> return next == ViolationAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ViolationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("violation agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"violation agreement"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): ViolationAgreement {
        return ViolationAgreement(
            code = code,
            title = title,
            shiftCode = shiftCode,
            trailerId = trailerId,
            driverId = driverId,
            active = active,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a violation agreement in status open. */
fun newViolationAgreement(code: String, title: String): ViolationAgreement {
    return ViolationAgreement(
        code = code,
        title = title,
        shiftCode = "",
        trailerId = "",
        driverId = "",
        active = false,
        amount = 0.0,
        status = ViolationAgreementStatus.OPEN,
    )
}
