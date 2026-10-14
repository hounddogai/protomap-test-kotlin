package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending lane plan checkpoint is in its life cycle. */
enum class PendingLanePlanCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending lane plan checkpoint and its current status. */
data class PendingLanePlanCheckpoint(
    var code: String,
    var label: String,
    var revision: Int,
    var tags: MutableList<String>,
    var lane: String,
    var country: String,
    var startsAt: Int,
    var contactEmail: String,
    var visible: Boolean,
    var summary: String,
    var limit: Int,
    var status: PendingLanePlanCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLanePlanCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLanePlanCheckpointStatus): Boolean {
        when (status) {
            PendingLanePlanCheckpointStatus.OPEN -> {
                when (next) {
                    PendingLanePlanCheckpointStatus.IN_REVIEW, PendingLanePlanCheckpointStatus.RESOLVED -> return true
                }
                return false
            }
            PendingLanePlanCheckpointStatus.IN_REVIEW -> return next == PendingLanePlanCheckpointStatus.RESOLVED
            PendingLanePlanCheckpointStatus.RESOLVED -> return next == PendingLanePlanCheckpointStatus.REOPENED || next == PendingLanePlanCheckpointStatus.ARCHIVED
            PendingLanePlanCheckpointStatus.REOPENED -> {
                when (next) {
                    PendingLanePlanCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingLanePlanCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("pending lane plan checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((revision.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): PendingLanePlanCheckpoint {
        return PendingLanePlanCheckpoint(
            code = code,
            label = label,
            revision = revision,
            tags = tags,
            lane = lane,
            country = country,
            startsAt = startsAt,
            contactEmail = contactEmail,
            visible = visible,
            summary = summary,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending lane plan checkpoint in status open. */
fun newPendingLanePlanCheckpoint(code: String, label: String): PendingLanePlanCheckpoint {
    return PendingLanePlanCheckpoint(
        code = code,
        label = label,
        revision = 0,
        tags = mutableListOf<String>(),
        lane = "",
        country = "",
        startsAt = 0,
        contactEmail = "",
        visible = false,
        summary = "",
        limit = 0,
        status = PendingLanePlanCheckpointStatus.OPEN,
    )
}
