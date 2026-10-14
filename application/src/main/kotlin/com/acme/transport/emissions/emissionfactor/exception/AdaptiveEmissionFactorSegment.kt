package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive emission factor segment is in its life cycle. */
enum class AdaptiveEmissionFactorSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive emission factor segment, with what the planners need to act on it. */
data class AdaptiveEmissionFactorSegment(
    var code: String,
    var label: String,
    var maximum: Double,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var carrier: String,
    var revision: Int,
    var status: AdaptiveEmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorSegmentStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorSegmentStatus.OPEN -> {
                when (next) {
                    AdaptiveEmissionFactorSegmentStatus.RESOLVED, AdaptiveEmissionFactorSegmentStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorSegmentStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveEmissionFactorSegmentStatus.RESOLVED, AdaptiveEmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorSegmentStatus.RESOLVED -> return next == AdaptiveEmissionFactorSegmentStatus.REOPENED
            AdaptiveEmissionFactorSegmentStatus.REOPENED -> return next == AdaptiveEmissionFactorSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor segment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(maximum * revision.toDouble() * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorSegment {
        return AdaptiveEmissionFactorSegment(
            code = code,
            label = label,
            maximum = maximum,
            unit = unit,
            attributes = attributes,
            carrier = carrier,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive emission factor segment in status open. */
fun newAdaptiveEmissionFactorSegment(code: String, label: String): AdaptiveEmissionFactorSegment {
    return AdaptiveEmissionFactorSegment(
        code = code,
        label = label,
        maximum = 0.0,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        carrier = "",
        revision = 0,
        status = AdaptiveEmissionFactorSegmentStatus.OPEN,
    )
}
