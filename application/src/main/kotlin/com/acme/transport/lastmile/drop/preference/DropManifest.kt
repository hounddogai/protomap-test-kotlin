package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop manifest is in its life cycle. */
enum class DropManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A drop manifest. Its code is unique within a tenant. */
data class DropManifest(
    var code: String,
    var label: String,
    var startsAt: Int,
    var notes: String,
    var createdAt: Int,
    var attempts: Int,
    var discount: Double,
    var lane: String,
    var ratio: Double,
    var trailerId: String,
    var status: DropManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DropManifestStatus): Boolean {
        when (status) {
            DropManifestStatus.PROPOSED -> return next == DropManifestStatus.WITHDRAWN
            DropManifestStatus.APPROVED -> {
                when (next) {
                    DropManifestStatus.REJECTED, DropManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            DropManifestStatus.REJECTED -> {
                when (next) {
                    DropManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropManifestStatus) {
        if (!canMoveTo(next)) {
            error("drop manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"drop manifest"} $code"

    fun weightedExposure(): Double = round(discount * startsAt.toDouble() * 100) / 100

    fun snapshot(): DropManifest {
        return DropManifest(
            code = code,
            label = label,
            startsAt = startsAt,
            notes = notes,
            createdAt = createdAt,
            attempts = attempts,
            discount = discount,
            lane = lane,
            ratio = ratio,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a drop manifest in status proposed. */
fun newDropManifest(code: String, label: String): DropManifest {
    return DropManifest(
        code = code,
        label = label,
        startsAt = 0,
        notes = "",
        createdAt = 0,
        attempts = 0,
        discount = 0.0,
        lane = "",
        ratio = 0.0,
        trailerId = "",
        status = DropManifestStatus.PROPOSED,
    )
}
