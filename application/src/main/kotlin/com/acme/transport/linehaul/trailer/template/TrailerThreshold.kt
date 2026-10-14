package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer threshold is in its life cycle. */
enum class TrailerThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A trailer threshold as the trailer team keeps it. */
data class TrailerThreshold(
    var code: String,
    var name: String,
    var margin: Double,
    var version: Int,
    var ownerId: String,
    var contactEmail: String,
    var tags: MutableList<String>,
    var attempts: Int,
    var updatedAt: Int,
    var durationMinutes: Int,
    var status: TrailerThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerThresholdStatus): Boolean {
        when (status) {
            TrailerThresholdStatus.CREATED -> {
                when (next) {
                    TrailerThresholdStatus.VALIDATED -> return true
                }
                return false
            }
            TrailerThresholdStatus.VALIDATED -> return next == TrailerThresholdStatus.RETIRED
            TrailerThresholdStatus.PUBLISHED -> return next == TrailerThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerThresholdStatus) {
        if (!canMoveTo(next)) {
            error("trailer threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((updatedAt.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TrailerThreshold {
        return TrailerThreshold(
            code = code,
            name = name,
            margin = margin,
            version = version,
            ownerId = ownerId,
            contactEmail = contactEmail,
            tags = tags,
            attempts = attempts,
            updatedAt = updatedAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer threshold in status created. */
fun newTrailerThreshold(code: String, name: String): TrailerThreshold {
    return TrailerThreshold(
        code = code,
        name = name,
        margin = 0.0,
        version = 0,
        ownerId = "",
        contactEmail = "",
        tags = mutableListOf<String>(),
        attempts = 0,
        updatedAt = 0,
        durationMinutes = 0,
        status = TrailerThresholdStatus.CREATED,
    )
}
