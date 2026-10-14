package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive availability manifest is in its life cycle. */
enum class AdaptiveAvailabilityManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One adaptive availability manifest, with what the planners need to act on it. */
data class AdaptiveAvailabilityManifest(
    var code: String,
    var label: String,
    var notes: String,
    var threshold: Double,
    var score: Double,
    var status: AdaptiveAvailabilityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAvailabilityManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAvailabilityManifestStatus): Boolean {
        when (status) {
            AdaptiveAvailabilityManifestStatus.PROPOSED -> return next == AdaptiveAvailabilityManifestStatus.APPROVED || next == AdaptiveAvailabilityManifestStatus.WITHDRAWN
            AdaptiveAvailabilityManifestStatus.APPROVED -> return next == AdaptiveAvailabilityManifestStatus.REJECTED
            AdaptiveAvailabilityManifestStatus.REJECTED -> return next == AdaptiveAvailabilityManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveAvailabilityManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive availability manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive availability manifest"} $code"

    fun adjustedVolume(): Double = round((score - threshold) * 100) / 100

    fun snapshot(): AdaptiveAvailabilityManifest {
        return AdaptiveAvailabilityManifest(
            code = code,
            label = label,
            notes = notes,
            threshold = threshold,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive availability manifest in status proposed. */
fun newAdaptiveAvailabilityManifest(code: String, label: String): AdaptiveAvailabilityManifest {
    return AdaptiveAvailabilityManifest(
        code = code,
        label = label,
        notes = "",
        threshold = 0.0,
        score = 0.0,
        status = AdaptiveAvailabilityManifestStatus.PROPOSED,
    )
}
