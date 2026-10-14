package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual roster agreement records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a manual roster agreement is in its life cycle. */
enum class ManualRosterAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual roster agreement as the roster team keeps it. */
data class ManualRosterAgreement(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var phone: String,
    var startsAt: Int,
    var capacity: Int,
    var status: ManualRosterAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRosterAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRosterAgreementStatus): Boolean {
        when (status) {
            ManualRosterAgreementStatus.NEW -> {
                when (next) {
                    ManualRosterAgreementStatus.RUNNING, ManualRosterAgreementStatus.DONE -> return true
                }
                return false
            }
            ManualRosterAgreementStatus.QUEUED -> {
                when (next) {
                    ManualRosterAgreementStatus.DONE -> return true
                }
                return false
            }
            ManualRosterAgreementStatus.RUNNING -> {
                when (next) {
                    ManualRosterAgreementStatus.FAILED -> return true
                }
                return false
            }
            ManualRosterAgreementStatus.DONE -> return next == ManualRosterAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualRosterAgreementStatus) {
        if (!canMoveTo(next)) {
            error("manual roster agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round(capacity.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): ManualRosterAgreement {
        return ManualRosterAgreement(
            code = code,
            label = label,
            expiresAt = expiresAt,
            phone = phone,
            startsAt = startsAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual roster agreement in status new. */
fun newManualRosterAgreement(code: String, label: String): ManualRosterAgreement {
    return ManualRosterAgreement(
        code = code,
        label = label,
        expiresAt = 0,
        phone = "",
        startsAt = 0,
        capacity = 0,
        status = ManualRosterAgreementStatus.NEW,
    )
}
