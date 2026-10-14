package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation threshold records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation threshold is in its life cycle. */
enum class ViolationThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A violation threshold. Its code is unique within a tenant. */
data class ViolationThreshold(
    var code: String,
    var name: String,
    var channel: String,
    var expiresAt: Int,
    var revision: Int,
    var title: String,
    var phone: String,
    var startsAt: Int,
    var attributes: MutableMap<String, String>,
    var status: ViolationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationThresholdStatus): Boolean {
        when (status) {
            ViolationThresholdStatus.PLANNED -> {
                when (next) {
                    ViolationThresholdStatus.SCHEDULED, ViolationThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            ViolationThresholdStatus.SCHEDULED -> return next == ViolationThresholdStatus.COMPLETED || next == ViolationThresholdStatus.CANCELLED
            ViolationThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    ViolationThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            ViolationThresholdStatus.COMPLETED -> {
                when (next) {
                    ViolationThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("violation threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((startsAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ViolationThreshold {
        return ViolationThreshold(
            code = code,
            name = name,
            channel = channel,
            expiresAt = expiresAt,
            revision = revision,
            title = title,
            phone = phone,
            startsAt = startsAt,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a violation threshold in status planned. */
fun newViolationThreshold(code: String, name: String): ViolationThreshold {
    return ViolationThreshold(
        code = code,
        name = name,
        channel = "",
        expiresAt = 0,
        revision = 0,
        title = "",
        phone = "",
        startsAt = 0,
        attributes = mutableMapOf<String, String>(),
        status = ViolationThresholdStatus.PLANNED,
    )
}
