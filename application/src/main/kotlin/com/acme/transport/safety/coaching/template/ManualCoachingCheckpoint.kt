package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual coaching checkpoint is in its life cycle. */
enum class ManualCoachingCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual coaching checkpoint and its current status. */
data class ManualCoachingCheckpoint(
    var code: String,
    var label: String,
    var windowDays: Int,
    var expiresAt: Int,
    var phone: String,
    var externalRef: String,
    var active: Boolean,
    var tags: MutableList<String>,
    var country: String,
    var approved: Boolean,
    var status: ManualCoachingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCoachingCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCoachingCheckpointStatus): Boolean {
        when (status) {
            ManualCoachingCheckpointStatus.CREATED -> return next == ManualCoachingCheckpointStatus.PUBLISHED
            ManualCoachingCheckpointStatus.VALIDATED -> {
                when (next) {
                    ManualCoachingCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualCoachingCheckpointStatus.PUBLISHED -> return next == ManualCoachingCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualCoachingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual coaching checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual coaching checkpoint"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round(expiresAt.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): ManualCoachingCheckpoint {
        return ManualCoachingCheckpoint(
            code = code,
            label = label,
            windowDays = windowDays,
            expiresAt = expiresAt,
            phone = phone,
            externalRef = externalRef,
            active = active,
            tags = tags,
            country = country,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual coaching checkpoint in status created. */
fun newManualCoachingCheckpoint(code: String, label: String): ManualCoachingCheckpoint {
    return ManualCoachingCheckpoint(
        code = code,
        label = label,
        windowDays = 0,
        expiresAt = 0,
        phone = "",
        externalRef = "",
        active = false,
        tags = mutableListOf<String>(),
        country = "",
        approved = false,
        status = ManualCoachingCheckpointStatus.CREATED,
    )
}
