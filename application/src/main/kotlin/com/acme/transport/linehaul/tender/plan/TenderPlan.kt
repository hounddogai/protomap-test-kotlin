package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.text.normalize

// The tender plan records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender plan is in its life cycle. */
enum class TenderPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tender plan. Its code is unique within a tenant. */
data class TenderPlan(
    var code: String,
    var title: String,
    var vehicleId: String,
    var startsAt: Int,
    var lane: String,
    var checksum: String,
    var attributes: MutableMap<String, String>,
    var status: TenderPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderPlanStatus): Boolean {
        when (status) {
            TenderPlanStatus.PLANNED -> return next == TenderPlanStatus.IN_PROGRESS
            TenderPlanStatus.SCHEDULED -> return next == TenderPlanStatus.IN_PROGRESS
            TenderPlanStatus.IN_PROGRESS -> {
                when (next) {
                    TenderPlanStatus.COMPLETED -> return true
                }
                return false
            }
            TenderPlanStatus.COMPLETED -> {
                when (next) {
                    TenderPlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderPlanStatus) {
        if (!canMoveTo(next)) {
            error("tender plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tender plan"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): TenderPlan {
        return TenderPlan(
            code = code,
            title = title,
            vehicleId = vehicleId,
            startsAt = startsAt,
            lane = lane,
            checksum = checksum,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tender plan in status planned. */
fun newTenderPlan(code: String, title: String): TenderPlan {
    return TenderPlan(
        code = code,
        title = title,
        vehicleId = "",
        startsAt = 0,
        lane = "",
        checksum = "",
        attributes = mutableMapOf<String, String>(),
        status = TenderPlanStatus.PLANNED,
    )
}
