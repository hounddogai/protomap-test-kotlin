package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dock door score is in its life cycle. */
enum class StrictDockDoorScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict dock door score. Its code is unique within a tenant. */
data class StrictDockDoorScore(
    var code: String,
    var label: String,
    var score: Double,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var attempts: Int,
    var windowDays: Int,
    var updatedAt: Int,
    var approved: Boolean,
    var tags: MutableList<String>,
    var status: StrictDockDoorScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorScoreStatus): Boolean {
        when (status) {
            StrictDockDoorScoreStatus.CREATED -> return next == StrictDockDoorScoreStatus.VALIDATED
            StrictDockDoorScoreStatus.VALIDATED -> {
                when (next) {
                    StrictDockDoorScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictDockDoorScoreStatus.PUBLISHED -> return next == StrictDockDoorScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictDockDoorScoreStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict dock door score"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round(odometer.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): StrictDockDoorScore {
        return StrictDockDoorScore(
            code = code,
            label = label,
            score = score,
            odometer = odometer,
            attributes = attributes,
            attempts = attempts,
            windowDays = windowDays,
            updatedAt = updatedAt,
            approved = approved,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict dock door score in status created. */
fun newStrictDockDoorScore(code: String, label: String): StrictDockDoorScore {
    return StrictDockDoorScore(
        code = code,
        label = label,
        score = 0.0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        attempts = 0,
        windowDays = 0,
        updatedAt = 0,
        approved = false,
        tags = mutableListOf<String>(),
        status = StrictDockDoorScoreStatus.CREATED,
    )
}
