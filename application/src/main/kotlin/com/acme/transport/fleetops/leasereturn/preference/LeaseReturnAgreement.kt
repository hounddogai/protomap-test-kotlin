package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize

/** Where a lease return agreement is in its life cycle. */
enum class LeaseReturnAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lease return agreement. Its code is unique within a tenant. */
data class LeaseReturnAgreement(
    var code: String,
    var title: String,
    var routeCode: String,
    var shiftCode: String,
    var approved: Boolean,
    var attempts: Int,
    var status: LeaseReturnAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnAgreementStatus): Boolean {
        when (status) {
            LeaseReturnAgreementStatus.IDLE -> return next == LeaseReturnAgreementStatus.READY
            LeaseReturnAgreementStatus.WARMING -> return next == LeaseReturnAgreementStatus.READY || next == LeaseReturnAgreementStatus.DRAINING
            LeaseReturnAgreementStatus.READY -> {
                when (next) {
                    LeaseReturnAgreementStatus.DRAINING, LeaseReturnAgreementStatus.STOPPED -> return true
                }
                return false
            }
            LeaseReturnAgreementStatus.DRAINING -> {
                when (next) {
                    LeaseReturnAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lease return agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): LeaseReturnAgreement {
        return LeaseReturnAgreement(
            code = code,
            title = title,
            routeCode = routeCode,
            shiftCode = shiftCode,
            approved = approved,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lease return agreement in status idle. */
fun newLeaseReturnAgreement(code: String, title: String): LeaseReturnAgreement {
    return LeaseReturnAgreement(
        code = code,
        title = title,
        routeCode = "",
        shiftCode = "",
        approved = false,
        attempts = 0,
        status = LeaseReturnAgreementStatus.IDLE,
    )
}
