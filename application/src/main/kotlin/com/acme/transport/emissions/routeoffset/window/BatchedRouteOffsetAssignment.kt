package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched route offset assignment is in its life cycle. */
enum class BatchedRouteOffsetAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a batched route offset assignment and its current status. */
data class BatchedRouteOffsetAssignment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var minimum: Double,
    var stopCount: Int,
    var capacity: Int,
    var tags: MutableList<String>,
    var status: BatchedRouteOffsetAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRouteOffsetAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRouteOffsetAssignmentStatus): Boolean {
        when (status) {
            BatchedRouteOffsetAssignmentStatus.CREATED -> return next == BatchedRouteOffsetAssignmentStatus.RETIRED
            BatchedRouteOffsetAssignmentStatus.VALIDATED -> {
                when (next) {
                    BatchedRouteOffsetAssignmentStatus.PUBLISHED, BatchedRouteOffsetAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            BatchedRouteOffsetAssignmentStatus.PUBLISHED -> {
                when (next) {
                    BatchedRouteOffsetAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRouteOffsetAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("batched route offset assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(minimum * stopCount.toDouble() * 100) / 100

    fun snapshot(): BatchedRouteOffsetAssignment {
        return BatchedRouteOffsetAssignment(
            code = code,
            title = title,
            pinned = pinned,
            minimum = minimum,
            stopCount = stopCount,
            capacity = capacity,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched route offset assignment in status created. */
fun newBatchedRouteOffsetAssignment(code: String, title: String): BatchedRouteOffsetAssignment {
    return BatchedRouteOffsetAssignment(
        code = code,
        title = title,
        pinned = false,
        minimum = 0.0,
        stopCount = 0,
        capacity = 0,
        tags = mutableListOf<String>(),
        status = BatchedRouteOffsetAssignmentStatus.CREATED,
    )
}
