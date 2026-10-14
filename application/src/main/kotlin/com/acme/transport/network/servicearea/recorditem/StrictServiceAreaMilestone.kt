package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict service area milestone is in its life cycle. */
enum class StrictServiceAreaMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict service area milestone. Its code is unique within a tenant. */
data class StrictServiceAreaMilestone(
    var code: String,
    var label: String,
    var discount: Double,
    var name: String,
    var timezone: String,
    var approved: Boolean,
    var routeCode: String,
    var minimum: Double,
    var tags: MutableList<String>,
    var expiresAt: Int,
    var status: StrictServiceAreaMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaMilestoneStatus): Boolean {
        when (status) {
            StrictServiceAreaMilestoneStatus.CREATED -> return next == StrictServiceAreaMilestoneStatus.VALIDATED || next == StrictServiceAreaMilestoneStatus.PUBLISHED
            StrictServiceAreaMilestoneStatus.VALIDATED -> return next == StrictServiceAreaMilestoneStatus.PUBLISHED
            StrictServiceAreaMilestoneStatus.PUBLISHED -> {
                when (next) {
                    StrictServiceAreaMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict service area milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round((minimum - discount) * 100) / 100

    fun snapshot(): StrictServiceAreaMilestone {
        return StrictServiceAreaMilestone(
            code = code,
            label = label,
            discount = discount,
            name = name,
            timezone = timezone,
            approved = approved,
            routeCode = routeCode,
            minimum = minimum,
            tags = tags,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict service area milestone in status created. */
fun newStrictServiceAreaMilestone(code: String, label: String): StrictServiceAreaMilestone {
    return StrictServiceAreaMilestone(
        code = code,
        label = label,
        discount = 0.0,
        name = "",
        timezone = "",
        approved = false,
        routeCode = "",
        minimum = 0.0,
        tags = mutableListOf<String>(),
        expiresAt = 0,
        status = StrictServiceAreaMilestoneStatus.CREATED,
    )
}
