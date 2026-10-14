package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback violation adjustment is in its life cycle. */
enum class FallbackViolationAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback violation adjustment as the violation team keeps it. */
data class FallbackViolationAdjustment(
    var code: String,
    var label: String,
    var ratio: Double,
    var contactEmail: String,
    var durationMinutes: Int,
    var minimum: Double,
    var version: Int,
    var channel: String,
    var reviewer: String,
    var description: String,
    var attributes: MutableMap<String, String>,
    var status: FallbackViolationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackViolationAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackViolationAdjustmentStatus): Boolean {
        when (status) {
            FallbackViolationAdjustmentStatus.NEW -> {
                when (next) {
                    FallbackViolationAdjustmentStatus.RUNNING -> return true
                }
                return false
            }
            FallbackViolationAdjustmentStatus.QUEUED -> return next == FallbackViolationAdjustmentStatus.RUNNING || next == FallbackViolationAdjustmentStatus.DONE
            FallbackViolationAdjustmentStatus.RUNNING -> {
                when (next) {
                    FallbackViolationAdjustmentStatus.DONE, FallbackViolationAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            FallbackViolationAdjustmentStatus.DONE -> {
                when (next) {
                    FallbackViolationAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackViolationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback violation adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"fallback violation adjustment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((durationMinutes.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): FallbackViolationAdjustment {
        return FallbackViolationAdjustment(
            code = code,
            label = label,
            ratio = ratio,
            contactEmail = contactEmail,
            durationMinutes = durationMinutes,
            minimum = minimum,
            version = version,
            channel = channel,
            reviewer = reviewer,
            description = description,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback violation adjustment in status new. */
fun newFallbackViolationAdjustment(code: String, label: String): FallbackViolationAdjustment {
    return FallbackViolationAdjustment(
        code = code,
        label = label,
        ratio = 0.0,
        contactEmail = "",
        durationMinutes = 0,
        minimum = 0.0,
        version = 0,
        channel = "",
        reviewer = "",
        description = "",
        attributes = mutableMapOf<String, String>(),
        status = FallbackViolationAdjustmentStatus.NEW,
    )
}
