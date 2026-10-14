package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt threshold records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt threshold is in its life cycle. */
enum class AttemptThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A attempt threshold as the attempt team keeps it. */
data class AttemptThreshold(
    var code: String,
    var title: String,
    var capacity: Int,
    var windowDays: Int,
    var priority: Int,
    var ownerId: String,
    var visible: Boolean,
    var phone: String,
    var unit: String,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var status: AttemptThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptThresholdStatus): Boolean {
        when (status) {
            AttemptThresholdStatus.PLANNED -> return next == AttemptThresholdStatus.SCHEDULED
            AttemptThresholdStatus.SCHEDULED -> {
                when (next) {
                    AttemptThresholdStatus.IN_PROGRESS, AttemptThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            AttemptThresholdStatus.IN_PROGRESS -> return next == AttemptThresholdStatus.COMPLETED || next == AttemptThresholdStatus.CANCELLED
            AttemptThresholdStatus.COMPLETED -> {
                when (next) {
                    AttemptThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptThresholdStatus) {
        if (!canMoveTo(next)) {
            error("attempt threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt threshold"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((priority.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): AttemptThreshold {
        return AttemptThreshold(
            code = code,
            title = title,
            capacity = capacity,
            windowDays = windowDays,
            priority = priority,
            ownerId = ownerId,
            visible = visible,
            phone = phone,
            unit = unit,
            pinned = pinned,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a attempt threshold in status planned. */
fun newAttemptThreshold(code: String, title: String): AttemptThreshold {
    return AttemptThreshold(
        code = code,
        title = title,
        capacity = 0,
        windowDays = 0,
        priority = 0,
        ownerId = "",
        visible = false,
        phone = "",
        unit = "",
        pinned = false,
        tags = mutableListOf<String>(),
        status = AttemptThresholdStatus.PLANNED,
    )
}
