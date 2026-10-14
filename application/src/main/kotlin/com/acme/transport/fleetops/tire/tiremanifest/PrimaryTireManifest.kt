package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary tire manifest records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a primary tire manifest is in its life cycle. */
enum class PrimaryTireManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary tire manifest as the tire team keeps it. */
data class PrimaryTireManifest(
    var code: String,
    var label: String,
    var vehicleId: String,
    var discount: Double,
    var minimum: Double,
    var batchSize: Int,
    var dock: String,
    var updatedAt: Int,
    var tags: MutableList<String>,
    var expiresAt: Int,
    var margin: Double,
    var status: PrimaryTireManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTireManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTireManifestStatus): Boolean {
        when (status) {
            PrimaryTireManifestStatus.PROPOSED -> {
                when (next) {
                    PrimaryTireManifestStatus.REJECTED, PrimaryTireManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTireManifestStatus.APPROVED -> return next == PrimaryTireManifestStatus.WITHDRAWN
            PrimaryTireManifestStatus.REJECTED -> return next == PrimaryTireManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryTireManifestStatus) {
        if (!canMoveTo(next)) {
            error("primary tire manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary tire manifest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((minimum + batchSize.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTireManifest {
        return PrimaryTireManifest(
            code = code,
            label = label,
            vehicleId = vehicleId,
            discount = discount,
            minimum = minimum,
            batchSize = batchSize,
            dock = dock,
            updatedAt = updatedAt,
            tags = tags,
            expiresAt = expiresAt,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary tire manifest in status proposed. */
fun newPrimaryTireManifest(code: String, label: String): PrimaryTireManifest {
    return PrimaryTireManifest(
        code = code,
        label = label,
        vehicleId = "",
        discount = 0.0,
        minimum = 0.0,
        batchSize = 0,
        dock = "",
        updatedAt = 0,
        tags = mutableListOf<String>(),
        expiresAt = 0,
        margin = 0.0,
        status = PrimaryTireManifestStatus.PROPOSED,
    )
}
