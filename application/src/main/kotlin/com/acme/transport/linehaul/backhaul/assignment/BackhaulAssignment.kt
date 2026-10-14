package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul assignment records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul assignment is in its life cycle. */
enum class BackhaulAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One backhaul assignment, with what the planners need to act on it. */
data class BackhaulAssignment(
    var code: String,
    var label: String,
    var dock: String,
    var etaMinutes: Int,
    var weight: Double,
    var threshold: Double,
    var active: Boolean,
    var amount: Double,
    var endsAt: Int,
    var lane: String,
    var depot: String,
    var status: BackhaulAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulAssignmentStatus): Boolean {
        when (status) {
            BackhaulAssignmentStatus.OPEN -> return next == BackhaulAssignmentStatus.ARCHIVED
            BackhaulAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    BackhaulAssignmentStatus.RESOLVED, BackhaulAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            BackhaulAssignmentStatus.RESOLVED -> {
                when (next) {
                    BackhaulAssignmentStatus.REOPENED, BackhaulAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BackhaulAssignmentStatus.REOPENED -> return next == BackhaulAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BackhaulAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("backhaul assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"backhaul assignment"} $code"

    fun netExposure(): Double = round((etaMinutes.toDouble() + amount) * 100) / 100

    fun snapshot(): BackhaulAssignment {
        return BackhaulAssignment(
            code = code,
            label = label,
            dock = dock,
            etaMinutes = etaMinutes,
            weight = weight,
            threshold = threshold,
            active = active,
            amount = amount,
            endsAt = endsAt,
            lane = lane,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a backhaul assignment in status open. */
fun newBackhaulAssignment(code: String, label: String): BackhaulAssignment {
    return BackhaulAssignment(
        code = code,
        label = label,
        dock = "",
        etaMinutes = 0,
        weight = 0.0,
        threshold = 0.0,
        active = false,
        amount = 0.0,
        endsAt = 0,
        lane = "",
        depot = "",
        status = BackhaulAssignmentStatus.OPEN,
    )
}
