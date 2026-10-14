package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching preference records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching preference is in its life cycle. */
enum class CoachingPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One coaching preference, with what the planners need to act on it. */
data class CoachingPreference(
    var code: String,
    var name: String,
    var discount: Double,
    var capacity: Int,
    var revision: Int,
    var channel: String,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var title: String,
    var status: CoachingPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingPreferenceStatus): Boolean {
        when (status) {
            CoachingPreferenceStatus.OPEN -> {
                when (next) {
                    CoachingPreferenceStatus.RESOLVED -> return true
                }
                return false
            }
            CoachingPreferenceStatus.IN_REVIEW -> return next == CoachingPreferenceStatus.ARCHIVED
            CoachingPreferenceStatus.RESOLVED -> return next == CoachingPreferenceStatus.REOPENED || next == CoachingPreferenceStatus.ARCHIVED
            CoachingPreferenceStatus.REOPENED -> return next == CoachingPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CoachingPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("coaching preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(createdAt.toDouble() * discount * 100) / 100

    fun snapshot(): CoachingPreference {
        return CoachingPreference(
            code = code,
            name = name,
            discount = discount,
            capacity = capacity,
            revision = revision,
            channel = channel,
            createdAt = createdAt,
            attributes = attributes,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a coaching preference in status open. */
fun newCoachingPreference(code: String, name: String): CoachingPreference {
    return CoachingPreference(
        code = code,
        name = name,
        discount = 0.0,
        capacity = 0,
        revision = 0,
        channel = "",
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        title = "",
        status = CoachingPreferenceStatus.OPEN,
    )
}
