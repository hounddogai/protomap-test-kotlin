package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual clearance checkpoint records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a manual clearance checkpoint is in its life cycle. */
enum class ManualClearanceCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual clearance checkpoint, with what the planners need to act on it. */
data class ManualClearanceCheckpoint(
    var code: String,
    var name: String,
    var discount: Double,
    var graceDays: Int,
    var title: String,
    var threshold: Double,
    var visible: Boolean,
    var weight: Double,
    var tags: MutableList<String>,
    var capacity: Int,
    var routeCode: String,
    var status: ManualClearanceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualClearanceCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualClearanceCheckpointStatus): Boolean {
        when (status) {
            ManualClearanceCheckpointStatus.DRAFT -> {
                when (next) {
                    ManualClearanceCheckpointStatus.PENDING -> return true
                }
                return false
            }
            ManualClearanceCheckpointStatus.PENDING -> {
                when (next) {
                    ManualClearanceCheckpointStatus.ACTIVE, ManualClearanceCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            ManualClearanceCheckpointStatus.ACTIVE -> return next == ManualClearanceCheckpointStatus.CLOSED
            ManualClearanceCheckpointStatus.SUSPENDED -> {
                when (next) {
                    ManualClearanceCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualClearanceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual clearance checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round(capacity.toDouble() * threshold * 100) / 100

    fun snapshot(): ManualClearanceCheckpoint {
        return ManualClearanceCheckpoint(
            code = code,
            name = name,
            discount = discount,
            graceDays = graceDays,
            title = title,
            threshold = threshold,
            visible = visible,
            weight = weight,
            tags = tags,
            capacity = capacity,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual clearance checkpoint in status draft. */
fun newManualClearanceCheckpoint(code: String, name: String): ManualClearanceCheckpoint {
    return ManualClearanceCheckpoint(
        code = code,
        name = name,
        discount = 0.0,
        graceDays = 0,
        title = "",
        threshold = 0.0,
        visible = false,
        weight = 0.0,
        tags = mutableListOf<String>(),
        capacity = 0,
        routeCode = "",
        status = ManualClearanceCheckpointStatus.DRAFT,
    )
}
