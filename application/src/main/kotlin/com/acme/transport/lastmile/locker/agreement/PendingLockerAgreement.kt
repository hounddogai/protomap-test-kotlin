package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending locker agreement is in its life cycle. */
enum class PendingLockerAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending locker agreement. Its code is unique within a tenant. */
data class PendingLockerAgreement(
    var code: String,
    var title: String,
    var ownerId: String,
    var unit: String,
    var maximum: Double,
    var contactEmail: String,
    var etaMinutes: Int,
    var status: PendingLockerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLockerAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLockerAgreementStatus): Boolean {
        when (status) {
            PendingLockerAgreementStatus.NEW -> {
                when (next) {
                    PendingLockerAgreementStatus.RUNNING, PendingLockerAgreementStatus.FAILED -> return true
                }
                return false
            }
            PendingLockerAgreementStatus.QUEUED -> {
                when (next) {
                    PendingLockerAgreementStatus.DONE, PendingLockerAgreementStatus.FAILED -> return true
                }
                return false
            }
            PendingLockerAgreementStatus.RUNNING -> return next == PendingLockerAgreementStatus.DONE
            PendingLockerAgreementStatus.DONE -> {
                when (next) {
                    PendingLockerAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingLockerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("pending locker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() - maximum) * 100) / 100

    fun snapshot(): PendingLockerAgreement {
        return PendingLockerAgreement(
            code = code,
            title = title,
            ownerId = ownerId,
            unit = unit,
            maximum = maximum,
            contactEmail = contactEmail,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending locker agreement in status new. */
fun newPendingLockerAgreement(code: String, title: String): PendingLockerAgreement {
    return PendingLockerAgreement(
        code = code,
        title = title,
        ownerId = "",
        unit = "",
        maximum = 0.0,
        contactEmail = "",
        etaMinutes = 0,
        status = PendingLockerAgreementStatus.NEW,
    )
}
