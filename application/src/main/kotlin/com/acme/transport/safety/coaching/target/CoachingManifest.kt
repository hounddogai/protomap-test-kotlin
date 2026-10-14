package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching manifest is in its life cycle. */
enum class CoachingManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a coaching manifest and its current status. */
data class CoachingManifest(
    var code: String,
    var name: String,
    var segment: String,
    var notes: String,
    var timezone: String,
    var fuelLiters: Double,
    var dock: String,
    var retries: Int,
    var threshold: Double,
    var quantity: Int,
    var status: CoachingManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingManifestStatus): Boolean {
        when (status) {
            CoachingManifestStatus.DRAFT -> return next == CoachingManifestStatus.SUSPENDED || next == CoachingManifestStatus.CLOSED
            CoachingManifestStatus.PENDING -> {
                when (next) {
                    CoachingManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            CoachingManifestStatus.ACTIVE -> return next == CoachingManifestStatus.SUSPENDED
            CoachingManifestStatus.SUSPENDED -> return next == CoachingManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CoachingManifestStatus) {
        if (!canMoveTo(next)) {
            error("coaching manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching manifest"} $code"

    fun netVolume(): Double = round(threshold * retries.toDouble() * 100) / 100

    fun snapshot(): CoachingManifest {
        return CoachingManifest(
            code = code,
            name = name,
            segment = segment,
            notes = notes,
            timezone = timezone,
            fuelLiters = fuelLiters,
            dock = dock,
            retries = retries,
            threshold = threshold,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a coaching manifest in status draft. */
fun newCoachingManifest(code: String, name: String): CoachingManifest {
    return CoachingManifest(
        code = code,
        name = name,
        segment = "",
        notes = "",
        timezone = "",
        fuelLiters = 0.0,
        dock = "",
        retries = 0,
        threshold = 0.0,
        quantity = 0,
        status = CoachingManifestStatus.DRAFT,
    )
}
