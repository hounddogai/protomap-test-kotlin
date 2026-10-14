package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback clearance manifest is in its life cycle. */
enum class FallbackClearanceManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One fallback clearance manifest, with what the planners need to act on it. */
data class FallbackClearanceManifest(
    var code: String,
    var label: String,
    var capacity: Int,
    var loadKg: Double,
    var discount: Double,
    var region: String,
    var status: FallbackClearanceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearanceManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearanceManifestStatus): Boolean {
        when (status) {
            FallbackClearanceManifestStatus.PLANNED -> {
                when (next) {
                    FallbackClearanceManifestStatus.IN_PROGRESS, FallbackClearanceManifestStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackClearanceManifestStatus.SCHEDULED -> {
                when (next) {
                    FallbackClearanceManifestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FallbackClearanceManifestStatus.IN_PROGRESS -> return next == FallbackClearanceManifestStatus.COMPLETED
            FallbackClearanceManifestStatus.COMPLETED -> {
                when (next) {
                    FallbackClearanceManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackClearanceManifestStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback clearance manifest"} $code"

    fun effectiveTotal(): Double = round((capacity.toDouble() + loadKg) * 100) / 100

    fun snapshot(): FallbackClearanceManifest {
        return FallbackClearanceManifest(
            code = code,
            label = label,
            capacity = capacity,
            loadKg = loadKg,
            discount = discount,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback clearance manifest in status planned. */
fun newFallbackClearanceManifest(code: String, label: String): FallbackClearanceManifest {
    return FallbackClearanceManifest(
        code = code,
        label = label,
        capacity = 0,
        loadKg = 0.0,
        discount = 0.0,
        region = "",
        status = FallbackClearanceManifestStatus.PLANNED,
    )
}
