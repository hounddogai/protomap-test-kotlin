package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary availability threshold is in its life cycle. */
enum class PrimaryAvailabilityThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary availability threshold, with what the planners need to act on it. */
data class PrimaryAvailabilityThreshold(
    var code: String,
    var label: String,
    var archived: Boolean,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var loadKg: Double,
    var etaMinutes: Int,
    var graceDays: Int,
    var status: PrimaryAvailabilityThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAvailabilityThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAvailabilityThresholdStatus): Boolean {
        when (status) {
            PrimaryAvailabilityThresholdStatus.PROPOSED -> {
                when (next) {
                    PrimaryAvailabilityThresholdStatus.REJECTED, PrimaryAvailabilityThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryAvailabilityThresholdStatus.APPROVED -> {
                when (next) {
                    PrimaryAvailabilityThresholdStatus.REJECTED, PrimaryAvailabilityThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryAvailabilityThresholdStatus.REJECTED -> return next == PrimaryAvailabilityThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryAvailabilityThresholdStatus) {
        if (!canMoveTo(next)) {
            error("primary availability threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((etaMinutes.toDouble() - loadKg) * 100) / 100

    fun snapshot(): PrimaryAvailabilityThreshold {
        return PrimaryAvailabilityThreshold(
            code = code,
            label = label,
            archived = archived,
            sequence = sequence,
            attributes = attributes,
            rate = rate,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary availability threshold in status proposed. */
fun newPrimaryAvailabilityThreshold(code: String, label: String): PrimaryAvailabilityThreshold {
    return PrimaryAvailabilityThreshold(
        code = code,
        label = label,
        archived = false,
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        loadKg = 0.0,
        etaMinutes = 0,
        graceDays = 0,
        status = PrimaryAvailabilityThresholdStatus.PROPOSED,
    )
}
