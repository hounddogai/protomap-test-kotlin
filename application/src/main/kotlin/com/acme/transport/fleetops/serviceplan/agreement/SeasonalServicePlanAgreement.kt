package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.text.normalize

// The seasonal service plan agreement records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a seasonal service plan agreement is in its life cycle. */
enum class SeasonalServicePlanAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One seasonal service plan agreement, with what the planners need to act on it. */
data class SeasonalServicePlanAgreement(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var segment: String,
    var description: String,
    var status: SeasonalServicePlanAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServicePlanAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServicePlanAgreementStatus): Boolean {
        when (status) {
            SeasonalServicePlanAgreementStatus.NEW -> return next == SeasonalServicePlanAgreementStatus.QUEUED || next == SeasonalServicePlanAgreementStatus.FAILED
            SeasonalServicePlanAgreementStatus.QUEUED -> return next == SeasonalServicePlanAgreementStatus.FAILED
            SeasonalServicePlanAgreementStatus.RUNNING -> {
                when (next) {
                    SeasonalServicePlanAgreementStatus.DONE, SeasonalServicePlanAgreementStatus.FAILED -> return true
                }
                return false
            }
            SeasonalServicePlanAgreementStatus.DONE -> {
                when (next) {
                    SeasonalServicePlanAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServicePlanAgreementStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service plan agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): SeasonalServicePlanAgreement {
        return SeasonalServicePlanAgreement(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            segment = segment,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal service plan agreement in status new. */
fun newSeasonalServicePlanAgreement(code: String, name: String): SeasonalServicePlanAgreement {
    return SeasonalServicePlanAgreement(
        code = code,
        name = name,
        durationMinutes = 0,
        segment = "",
        description = "",
        status = SeasonalServicePlanAgreementStatus.NEW,
    )
}
