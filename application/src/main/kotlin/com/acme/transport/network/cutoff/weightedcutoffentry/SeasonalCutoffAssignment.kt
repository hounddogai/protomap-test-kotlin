package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal cutoff assignment is in its life cycle. */
enum class SeasonalCutoffAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal cutoff assignment. Its code is unique within a tenant. */
data class SeasonalCutoffAssignment(
    var code: String,
    var label: String,
    var amount: Double,
    var retries: Int,
    var threshold: Double,
    var tags: MutableList<String>,
    var limit: Int,
    var notes: String,
    var status: SeasonalCutoffAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCutoffAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCutoffAssignmentStatus): Boolean {
        when (status) {
            SeasonalCutoffAssignmentStatus.DRAFT -> return next == SeasonalCutoffAssignmentStatus.SUSPENDED || next == SeasonalCutoffAssignmentStatus.CLOSED
            SeasonalCutoffAssignmentStatus.PENDING -> return next == SeasonalCutoffAssignmentStatus.ACTIVE || next == SeasonalCutoffAssignmentStatus.CLOSED
            SeasonalCutoffAssignmentStatus.ACTIVE -> return next == SeasonalCutoffAssignmentStatus.SUSPENDED
            SeasonalCutoffAssignmentStatus.SUSPENDED -> return next == SeasonalCutoffAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalCutoffAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cutoff assignment $code cannot move from $status to $next")
        }
        status = next
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

    fun weightedValue(): Double = round((limit.toDouble() + amount) * 100) / 100

    fun snapshot(): SeasonalCutoffAssignment {
        return SeasonalCutoffAssignment(
            code = code,
            label = label,
            amount = amount,
            retries = retries,
            threshold = threshold,
            tags = tags,
            limit = limit,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal cutoff assignment in status draft. */
fun newSeasonalCutoffAssignment(code: String, label: String): SeasonalCutoffAssignment {
    return SeasonalCutoffAssignment(
        code = code,
        label = label,
        amount = 0.0,
        retries = 0,
        threshold = 0.0,
        tags = mutableListOf<String>(),
        limit = 0,
        notes = "",
        status = SeasonalCutoffAssignmentStatus.DRAFT,
    )
}
