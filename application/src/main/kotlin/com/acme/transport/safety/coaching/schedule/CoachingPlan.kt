package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.text.normalize

/** Where a coaching plan is in its life cycle. */
enum class CoachingPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A coaching plan as the coaching team keeps it. */
data class CoachingPlan(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var checksum: String,
    var quantity: Int,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var visible: Boolean,
    var status: CoachingPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingPlanStatus): Boolean {
        when (status) {
            CoachingPlanStatus.CREATED -> {
                when (next) {
                    CoachingPlanStatus.VALIDATED, CoachingPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            CoachingPlanStatus.VALIDATED -> {
                when (next) {
                    CoachingPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            CoachingPlanStatus.PUBLISHED -> {
                when (next) {
                    CoachingPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingPlanStatus) {
        if (!canMoveTo(next)) {
            error("coaching plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching plan"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CoachingPlan {
        return CoachingPlan(
            code = code,
            name = name,
            pinned = pinned,
            checksum = checksum,
            quantity = quantity,
            attributes = attributes,
            ownerId = ownerId,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching plan in status created. */
fun newCoachingPlan(code: String, name: String): CoachingPlan {
    return CoachingPlan(
        code = code,
        name = name,
        pinned = false,
        checksum = "",
        quantity = 0,
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        visible = false,
        status = CoachingPlanStatus.CREATED,
    )
}
