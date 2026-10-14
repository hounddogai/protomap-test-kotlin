package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy declaration manifest records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a legacy declaration manifest is in its life cycle. */
enum class LegacyDeclarationManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy declaration manifest, with what the planners need to act on it. */
data class LegacyDeclarationManifest(
    var code: String,
    var label: String,
    var ceiling: Double,
    var margin: Double,
    var revision: Int,
    var updatedAt: Int,
    var channel: String,
    var minimum: Double,
    var distanceKm: Double,
    var status: LegacyDeclarationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDeclarationManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDeclarationManifestStatus): Boolean {
        when (status) {
            LegacyDeclarationManifestStatus.IDLE -> {
                when (next) {
                    LegacyDeclarationManifestStatus.READY -> return true
                }
                return false
            }
            LegacyDeclarationManifestStatus.WARMING -> {
                when (next) {
                    LegacyDeclarationManifestStatus.READY -> return true
                }
                return false
            }
            LegacyDeclarationManifestStatus.READY -> {
                when (next) {
                    LegacyDeclarationManifestStatus.DRAINING -> return true
                }
                return false
            }
            LegacyDeclarationManifestStatus.DRAINING -> {
                when (next) {
                    LegacyDeclarationManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDeclarationManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy declaration manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((ceiling + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyDeclarationManifest {
        return LegacyDeclarationManifest(
            code = code,
            label = label,
            ceiling = ceiling,
            margin = margin,
            revision = revision,
            updatedAt = updatedAt,
            channel = channel,
            minimum = minimum,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy declaration manifest in status idle. */
fun newLegacyDeclarationManifest(code: String, label: String): LegacyDeclarationManifest {
    return LegacyDeclarationManifest(
        code = code,
        label = label,
        ceiling = 0.0,
        margin = 0.0,
        revision = 0,
        updatedAt = 0,
        channel = "",
        minimum = 0.0,
        distanceKm = 0.0,
        status = LegacyDeclarationManifestStatus.IDLE,
    )
}
