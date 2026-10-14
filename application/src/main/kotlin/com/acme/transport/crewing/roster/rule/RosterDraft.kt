package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster draft is in its life cycle. */
enum class RosterDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A roster draft. Its code is unique within a tenant. */
data class RosterDraft(
    var code: String,
    var label: String,
    var approved: Boolean,
    var routeCode: String,
    var carrier: String,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var expiresAt: Int,
    var minimum: Double,
    var sequence: Int,
    var status: RosterDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterDraftStatus): Boolean {
        when (status) {
            RosterDraftStatus.NEW -> return next == RosterDraftStatus.DONE
            RosterDraftStatus.QUEUED -> {
                when (next) {
                    RosterDraftStatus.DONE -> return true
                }
                return false
            }
            RosterDraftStatus.RUNNING -> {
                when (next) {
                    RosterDraftStatus.DONE -> return true
                }
                return false
            }
            RosterDraftStatus.DONE -> {
                when (next) {
                    RosterDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterDraftStatus) {
        if (!canMoveTo(next)) {
            error("roster draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(minimum * updatedAt.toDouble() * 100) / 100

    fun snapshot(): RosterDraft {
        return RosterDraft(
            code = code,
            label = label,
            approved = approved,
            routeCode = routeCode,
            carrier = carrier,
            updatedAt = updatedAt,
            attributes = attributes,
            archived = archived,
            expiresAt = expiresAt,
            minimum = minimum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster draft in status new. */
fun newRosterDraft(code: String, label: String): RosterDraft {
    return RosterDraft(
        code = code,
        label = label,
        approved = false,
        routeCode = "",
        carrier = "",
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        archived = false,
        expiresAt = 0,
        minimum = 0.0,
        sequence = 0,
        status = RosterDraftStatus.NEW,
    )
}
