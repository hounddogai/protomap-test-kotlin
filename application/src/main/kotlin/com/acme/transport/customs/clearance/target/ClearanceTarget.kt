package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance target records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance target is in its life cycle. */
enum class ClearanceTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a clearance target and its current status. */
data class ClearanceTarget(
    var code: String,
    var title: String,
    var label: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var source: String,
    var archived: Boolean,
    var margin: Double,
    var pallets: Int,
    var status: ClearanceTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceTargetStatus): Boolean {
        when (status) {
            ClearanceTargetStatus.DRAFT -> {
                when (next) {
                    ClearanceTargetStatus.PENDING, ClearanceTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            ClearanceTargetStatus.PENDING -> {
                when (next) {
                    ClearanceTargetStatus.ACTIVE, ClearanceTargetStatus.CLOSED -> return true
                }
                return false
            }
            ClearanceTargetStatus.ACTIVE -> return next == ClearanceTargetStatus.SUSPENDED
            ClearanceTargetStatus.SUSPENDED -> {
                when (next) {
                    ClearanceTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceTargetStatus) {
        if (!canMoveTo(next)) {
            error("clearance target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((pallets.toDouble() + margin) * 100) / 100

    fun snapshot(): ClearanceTarget {
        return ClearanceTarget(
            code = code,
            title = title,
            label = label,
            tags = tags,
            reviewer = reviewer,
            source = source,
            archived = archived,
            margin = margin,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a clearance target in status draft. */
fun newClearanceTarget(code: String, title: String): ClearanceTarget {
    return ClearanceTarget(
        code = code,
        title = title,
        label = "",
        tags = mutableListOf<String>(),
        reviewer = "",
        source = "",
        archived = false,
        margin = 0.0,
        pallets = 0,
        status = ClearanceTargetStatus.DRAFT,
    )
}
