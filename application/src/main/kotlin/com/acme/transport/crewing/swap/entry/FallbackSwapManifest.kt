package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback swap manifest is in its life cycle. */
enum class FallbackSwapManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback swap manifest, with what the planners need to act on it. */
data class FallbackSwapManifest(
    var code: String,
    var title: String,
    var trailerId: String,
    var updatedAt: Int,
    var currency: String,
    var contactEmail: String,
    var region: String,
    var minimum: Double,
    var driverId: String,
    var windowDays: Int,
    var approved: Boolean,
    var status: FallbackSwapManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSwapManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSwapManifestStatus): Boolean {
        when (status) {
            FallbackSwapManifestStatus.IDLE -> {
                when (next) {
                    FallbackSwapManifestStatus.WARMING -> return true
                }
                return false
            }
            FallbackSwapManifestStatus.WARMING -> {
                when (next) {
                    FallbackSwapManifestStatus.READY, FallbackSwapManifestStatus.STOPPED -> return true
                }
                return false
            }
            FallbackSwapManifestStatus.READY -> return next == FallbackSwapManifestStatus.STOPPED
            FallbackSwapManifestStatus.DRAINING -> {
                when (next) {
                    FallbackSwapManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackSwapManifestStatus) {
        if (!canMoveTo(next)) {
            error("fallback swap manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback swap manifest"} $code"

    fun weightedVolume(): Double = round(updatedAt.toDouble() * minimum * 100) / 100

    fun snapshot(): FallbackSwapManifest {
        return FallbackSwapManifest(
            code = code,
            title = title,
            trailerId = trailerId,
            updatedAt = updatedAt,
            currency = currency,
            contactEmail = contactEmail,
            region = region,
            minimum = minimum,
            driverId = driverId,
            windowDays = windowDays,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback swap manifest in status idle. */
fun newFallbackSwapManifest(code: String, title: String): FallbackSwapManifest {
    return FallbackSwapManifest(
        code = code,
        title = title,
        trailerId = "",
        updatedAt = 0,
        currency = "",
        contactEmail = "",
        region = "",
        minimum = 0.0,
        driverId = "",
        windowDays = 0,
        approved = false,
        status = FallbackSwapManifestStatus.IDLE,
    )
}
