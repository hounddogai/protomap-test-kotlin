package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive attempt manifest is in its life cycle. */
enum class AdaptiveAttemptManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive attempt manifest, with what the planners need to act on it. */
data class AdaptiveAttemptManifest(
    var code: String,
    var title: String,
    var region: String,
    var driverId: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var shiftCode: String,
    var currency: String,
    var name: String,
    var visible: Boolean,
    var status: AdaptiveAttemptManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAttemptManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAttemptManifestStatus): Boolean {
        when (status) {
            AdaptiveAttemptManifestStatus.PLANNED -> {
                when (next) {
                    AdaptiveAttemptManifestStatus.SCHEDULED, AdaptiveAttemptManifestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AdaptiveAttemptManifestStatus.SCHEDULED -> return next == AdaptiveAttemptManifestStatus.COMPLETED
            AdaptiveAttemptManifestStatus.IN_PROGRESS -> return next == AdaptiveAttemptManifestStatus.CANCELLED
            AdaptiveAttemptManifestStatus.COMPLETED -> {
                when (next) {
                    AdaptiveAttemptManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAttemptManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive attempt manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive attempt manifest"} $code"

    fun netExposure(): Double = round(fuelLiters * windowDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveAttemptManifest {
        return AdaptiveAttemptManifest(
            code = code,
            title = title,
            region = region,
            driverId = driverId,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            shiftCode = shiftCode,
            currency = currency,
            name = name,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive attempt manifest in status planned. */
fun newAdaptiveAttemptManifest(code: String, title: String): AdaptiveAttemptManifest {
    return AdaptiveAttemptManifest(
        code = code,
        title = title,
        region = "",
        driverId = "",
        fuelLiters = 0.0,
        windowDays = 0,
        shiftCode = "",
        currency = "",
        name = "",
        visible = false,
        status = AdaptiveAttemptManifestStatus.PLANNED,
    )
}
