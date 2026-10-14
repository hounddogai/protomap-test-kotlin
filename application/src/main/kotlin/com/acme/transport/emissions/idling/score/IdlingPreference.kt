package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.text.normalize

/** Where a idling preference is in its life cycle. */
enum class IdlingPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A idling preference as the idling team keeps it. */
data class IdlingPreference(
    var code: String,
    var title: String,
    var locale: String,
    var createdAt: Int,
    var tags: MutableList<String>,
    var status: IdlingPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingPreferenceStatus): Boolean {
        when (status) {
            IdlingPreferenceStatus.PLANNED -> return next == IdlingPreferenceStatus.IN_PROGRESS || next == IdlingPreferenceStatus.CANCELLED
            IdlingPreferenceStatus.SCHEDULED -> {
                when (next) {
                    IdlingPreferenceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            IdlingPreferenceStatus.IN_PROGRESS -> return next == IdlingPreferenceStatus.COMPLETED
            IdlingPreferenceStatus.COMPLETED -> return next == IdlingPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: IdlingPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("idling preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): IdlingPreference {
        return IdlingPreference(
            code = code,
            title = title,
            locale = locale,
            createdAt = createdAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a idling preference in status planned. */
fun newIdlingPreference(code: String, title: String): IdlingPreference {
    return IdlingPreference(
        code = code,
        title = title,
        locale = "",
        createdAt = 0,
        tags = mutableListOf<String>(),
        status = IdlingPreferenceStatus.PLANNED,
    )
}
