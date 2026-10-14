package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff manifest is in its life cycle. */
enum class CutoffManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One cutoff manifest, with what the planners need to act on it. */
data class CutoffManifest(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var floorValue: Double,
    var weight: Double,
    var externalRef: String,
    var pinned: Boolean,
    var endsAt: Int,
    var attempts: Int,
    var startsAt: Int,
    var ownerId: String,
    var status: CutoffManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffManifestStatus): Boolean {
        when (status) {
            CutoffManifestStatus.OPEN -> return next == CutoffManifestStatus.IN_REVIEW || next == CutoffManifestStatus.RESOLVED
            CutoffManifestStatus.IN_REVIEW -> return next == CutoffManifestStatus.RESOLVED || next == CutoffManifestStatus.ARCHIVED
            CutoffManifestStatus.RESOLVED -> return next == CutoffManifestStatus.REOPENED
            CutoffManifestStatus.REOPENED -> return next == CutoffManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CutoffManifestStatus) {
        if (!canMoveTo(next)) {
            error("cutoff manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((endsAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): CutoffManifest {
        return CutoffManifest(
            code = code,
            name = name,
            expiresAt = expiresAt,
            floorValue = floorValue,
            weight = weight,
            externalRef = externalRef,
            pinned = pinned,
            endsAt = endsAt,
            attempts = attempts,
            startsAt = startsAt,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff manifest in status open. */
fun newCutoffManifest(code: String, name: String): CutoffManifest {
    return CutoffManifest(
        code = code,
        name = name,
        expiresAt = 0,
        floorValue = 0.0,
        weight = 0.0,
        externalRef = "",
        pinned = false,
        endsAt = 0,
        attempts = 0,
        startsAt = 0,
        ownerId = "",
        status = CutoffManifestStatus.OPEN,
    )
}
