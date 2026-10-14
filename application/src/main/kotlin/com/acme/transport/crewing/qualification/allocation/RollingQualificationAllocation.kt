package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling qualification allocation records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a rolling qualification allocation is in its life cycle. */
enum class RollingQualificationAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling qualification allocation as the qualification team keeps it. */
data class RollingQualificationAllocation(
    var code: String,
    var title: String,
    var startsAt: Int,
    var retries: Int,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var contactEmail: String,
    var shiftCode: String,
    var status: RollingQualificationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingQualificationAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingQualificationAllocationStatus): Boolean {
        when (status) {
            RollingQualificationAllocationStatus.IDLE -> return next == RollingQualificationAllocationStatus.WARMING || next == RollingQualificationAllocationStatus.STOPPED
            RollingQualificationAllocationStatus.WARMING -> {
                when (next) {
                    RollingQualificationAllocationStatus.DRAINING, RollingQualificationAllocationStatus.STOPPED -> return true
                }
                return false
            }
            RollingQualificationAllocationStatus.READY -> {
                when (next) {
                    RollingQualificationAllocationStatus.DRAINING, RollingQualificationAllocationStatus.STOPPED -> return true
                }
                return false
            }
            RollingQualificationAllocationStatus.DRAINING -> {
                when (next) {
                    RollingQualificationAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingQualificationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling qualification allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((retries.toDouble() + budget) * 100) / 100

    fun snapshot(): RollingQualificationAllocation {
        return RollingQualificationAllocation(
            code = code,
            title = title,
            startsAt = startsAt,
            retries = retries,
            budget = budget,
            attributes = attributes,
            reviewer = reviewer,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling qualification allocation in status idle. */
fun newRollingQualificationAllocation(code: String, title: String): RollingQualificationAllocation {
    return RollingQualificationAllocation(
        code = code,
        title = title,
        startsAt = 0,
        retries = 0,
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        contactEmail = "",
        shiftCode = "",
        status = RollingQualificationAllocationStatus.IDLE,
    )
}
