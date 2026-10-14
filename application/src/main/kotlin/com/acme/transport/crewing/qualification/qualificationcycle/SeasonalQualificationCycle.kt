package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.normalize

// The seasonal qualification cycle records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a seasonal qualification cycle is in its life cycle. */
enum class SeasonalQualificationCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal qualification cycle, with what the planners need to act on it. */
data class SeasonalQualificationCycle(
    var code: String,
    var title: String,
    var description: String,
    var externalRef: String,
    var driverId: String,
    var quantity: Int,
    var status: SeasonalQualificationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalQualificationCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalQualificationCycleStatus): Boolean {
        when (status) {
            SeasonalQualificationCycleStatus.PROPOSED -> return next == SeasonalQualificationCycleStatus.APPROVED
            SeasonalQualificationCycleStatus.APPROVED -> {
                when (next) {
                    SeasonalQualificationCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalQualificationCycleStatus.REJECTED -> return next == SeasonalQualificationCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalQualificationCycleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal qualification cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): SeasonalQualificationCycle {
        return SeasonalQualificationCycle(
            code = code,
            title = title,
            description = description,
            externalRef = externalRef,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal qualification cycle in status proposed. */
fun newSeasonalQualificationCycle(code: String, title: String): SeasonalQualificationCycle {
    return SeasonalQualificationCycle(
        code = code,
        title = title,
        description = "",
        externalRef = "",
        driverId = "",
        quantity = 0,
        status = SeasonalQualificationCycleStatus.PROPOSED,
    )
}
