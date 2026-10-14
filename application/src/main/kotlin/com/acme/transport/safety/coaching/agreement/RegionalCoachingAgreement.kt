package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional coaching agreement records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a regional coaching agreement is in its life cycle. */
enum class RegionalCoachingAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional coaching agreement. Its code is unique within a tenant. */
data class RegionalCoachingAgreement(
    var code: String,
    var label: String,
    var unit: String,
    var title: String,
    var archived: Boolean,
    var retries: Int,
    var active: Boolean,
    var minimum: Double,
    var status: RegionalCoachingAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCoachingAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCoachingAgreementStatus): Boolean {
        when (status) {
            RegionalCoachingAgreementStatus.OPEN -> {
                when (next) {
                    RegionalCoachingAgreementStatus.REOPENED -> return true
                }
                return false
            }
            RegionalCoachingAgreementStatus.IN_REVIEW -> {
                when (next) {
                    RegionalCoachingAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalCoachingAgreementStatus.RESOLVED -> return next == RegionalCoachingAgreementStatus.ARCHIVED
            RegionalCoachingAgreementStatus.REOPENED -> {
                when (next) {
                    RegionalCoachingAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCoachingAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional coaching agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional coaching agreement"} $code"

    fun weightedValue(): Double = round((retries.toDouble() + minimum) * 100) / 100

    fun snapshot(): RegionalCoachingAgreement {
        return RegionalCoachingAgreement(
            code = code,
            label = label,
            unit = unit,
            title = title,
            archived = archived,
            retries = retries,
            active = active,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional coaching agreement in status open. */
fun newRegionalCoachingAgreement(code: String, label: String): RegionalCoachingAgreement {
    return RegionalCoachingAgreement(
        code = code,
        label = label,
        unit = "",
        title = "",
        archived = false,
        retries = 0,
        active = false,
        minimum = 0.0,
        status = RegionalCoachingAgreementStatus.OPEN,
    )
}
