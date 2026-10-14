package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional capacity assignment records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a regional capacity assignment is in its life cycle. */
enum class RegionalCapacityAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional capacity assignment. Its code is unique within a tenant. */
data class RegionalCapacityAssignment(
    var code: String,
    var title: String,
    var archived: Boolean,
    var sequence: Int,
    var priority: Int,
    var score: Double,
    var reviewer: String,
    var source: String,
    var dock: String,
    var status: RegionalCapacityAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityAssignmentStatus): Boolean {
        when (status) {
            RegionalCapacityAssignmentStatus.IDLE -> return next == RegionalCapacityAssignmentStatus.STOPPED
            RegionalCapacityAssignmentStatus.WARMING -> return next == RegionalCapacityAssignmentStatus.READY
            RegionalCapacityAssignmentStatus.READY -> return next == RegionalCapacityAssignmentStatus.DRAINING
            RegionalCapacityAssignmentStatus.DRAINING -> return next == RegionalCapacityAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalCapacityAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((score - priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalCapacityAssignment {
        return RegionalCapacityAssignment(
            code = code,
            title = title,
            archived = archived,
            sequence = sequence,
            priority = priority,
            score = score,
            reviewer = reviewer,
            source = source,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional capacity assignment in status idle. */
fun newRegionalCapacityAssignment(code: String, title: String): RegionalCapacityAssignment {
    return RegionalCapacityAssignment(
        code = code,
        title = title,
        archived = false,
        sequence = 0,
        priority = 0,
        score = 0.0,
        reviewer = "",
        source = "",
        dock = "",
        status = RegionalCapacityAssignmentStatus.IDLE,
    )
}
