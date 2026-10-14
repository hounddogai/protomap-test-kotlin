package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize

/** Where a inspection cycle is in its life cycle. */
enum class InspectionCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One inspection cycle, with what the planners need to act on it. */
data class InspectionCycle(
    var code: String,
    var title: String,
    var reviewer: String,
    var quantity: Int,
    var name: String,
    var phone: String,
    var shiftCode: String,
    var status: InspectionCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionCycleStatus): Boolean {
        when (status) {
            InspectionCycleStatus.PROPOSED -> {
                when (next) {
                    InspectionCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionCycleStatus.APPROVED -> return next == InspectionCycleStatus.REJECTED || next == InspectionCycleStatus.WITHDRAWN
            InspectionCycleStatus.REJECTED -> {
                when (next) {
                    InspectionCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionCycleStatus) {
        if (!canMoveTo(next)) {
            error("inspection cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): InspectionCycle {
        return InspectionCycle(
            code = code,
            title = title,
            reviewer = reviewer,
            quantity = quantity,
            name = name,
            phone = phone,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection cycle in status proposed. */
fun newInspectionCycle(code: String, title: String): InspectionCycle {
    return InspectionCycle(
        code = code,
        title = title,
        reviewer = "",
        quantity = 0,
        name = "",
        phone = "",
        shiftCode = "",
        status = InspectionCycleStatus.PROPOSED,
    )
}
