package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict incident draft is in its life cycle. */
enum class StrictIncidentDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict incident draft. Its code is unique within a tenant. */
data class StrictIncidentDraft(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var threshold: Double,
    var ceiling: Double,
    var graceDays: Int,
    var priority: Int,
    var archived: Boolean,
    var score: Double,
    var depot: String,
    var status: StrictIncidentDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIncidentDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIncidentDraftStatus): Boolean {
        when (status) {
            StrictIncidentDraftStatus.OPEN -> return next == StrictIncidentDraftStatus.RESOLVED
            StrictIncidentDraftStatus.IN_REVIEW -> return next == StrictIncidentDraftStatus.RESOLVED || next == StrictIncidentDraftStatus.REOPENED
            StrictIncidentDraftStatus.RESOLVED -> return next == StrictIncidentDraftStatus.ARCHIVED
            StrictIncidentDraftStatus.REOPENED -> return next == StrictIncidentDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictIncidentDraftStatus) {
        if (!canMoveTo(next)) {
            error("strict incident draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((ceiling - graceDays.toDouble()) * 100) / 100

    fun snapshot(): StrictIncidentDraft {
        return StrictIncidentDraft(
            code = code,
            name = name,
            tags = tags,
            threshold = threshold,
            ceiling = ceiling,
            graceDays = graceDays,
            priority = priority,
            archived = archived,
            score = score,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict incident draft in status open. */
fun newStrictIncidentDraft(code: String, name: String): StrictIncidentDraft {
    return StrictIncidentDraft(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        ceiling = 0.0,
        graceDays = 0,
        priority = 0,
        archived = false,
        score = 0.0,
        depot = "",
        status = StrictIncidentDraftStatus.OPEN,
    )
}
