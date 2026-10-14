package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global qualification slot is in its life cycle. */
enum class GlobalQualificationSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global qualification slot. Its code is unique within a tenant. */
data class GlobalQualificationSlot(
    var code: String,
    var label: String,
    var attempts: Int,
    var trailerId: String,
    var title: String,
    var reviewer: String,
    var startsAt: Int,
    var lane: String,
    var routeCode: String,
    var etaMinutes: Int,
    var status: GlobalQualificationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationSlotStatus): Boolean {
        when (status) {
            GlobalQualificationSlotStatus.PROPOSED -> return next == GlobalQualificationSlotStatus.WITHDRAWN
            GlobalQualificationSlotStatus.APPROVED -> return next == GlobalQualificationSlotStatus.REJECTED || next == GlobalQualificationSlotStatus.WITHDRAWN
            GlobalQualificationSlotStatus.REJECTED -> {
                when (next) {
                    GlobalQualificationSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalQualificationSlotStatus) {
        if (!canMoveTo(next)) {
            error("global qualification slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round((attempts.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): GlobalQualificationSlot {
        return GlobalQualificationSlot(
            code = code,
            label = label,
            attempts = attempts,
            trailerId = trailerId,
            title = title,
            reviewer = reviewer,
            startsAt = startsAt,
            lane = lane,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global qualification slot in status proposed. */
fun newGlobalQualificationSlot(code: String, label: String): GlobalQualificationSlot {
    return GlobalQualificationSlot(
        code = code,
        label = label,
        attempts = 0,
        trailerId = "",
        title = "",
        reviewer = "",
        startsAt = 0,
        lane = "",
        routeCode = "",
        etaMinutes = 0,
        status = GlobalQualificationSlotStatus.PROPOSED,
    )
}
