package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict tender profile is in its life cycle. */
enum class StrictTenderProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict tender profile, with what the planners need to act on it. */
data class StrictTenderProfile(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var capacity: Int,
    var budget: Double,
    var batchSize: Int,
    var status: StrictTenderProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTenderProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTenderProfileStatus): Boolean {
        when (status) {
            StrictTenderProfileStatus.DRAFT -> {
                when (next) {
                    StrictTenderProfileStatus.ACTIVE, StrictTenderProfileStatus.CLOSED -> return true
                }
                return false
            }
            StrictTenderProfileStatus.PENDING -> {
                when (next) {
                    StrictTenderProfileStatus.ACTIVE, StrictTenderProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictTenderProfileStatus.ACTIVE -> {
                when (next) {
                    StrictTenderProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictTenderProfileStatus.SUSPENDED -> return next == StrictTenderProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictTenderProfileStatus) {
        if (!canMoveTo(next)) {
            error("strict tender profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict tender profile"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((startsAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): StrictTenderProfile {
        return StrictTenderProfile(
            code = code,
            label = label,
            attributes = attributes,
            startsAt = startsAt,
            capacity = capacity,
            budget = budget,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict tender profile in status draft. */
fun newStrictTenderProfile(code: String, label: String): StrictTenderProfile {
    return StrictTenderProfile(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        capacity = 0,
        budget = 0.0,
        batchSize = 0,
        status = StrictTenderProfileStatus.DRAFT,
    )
}
