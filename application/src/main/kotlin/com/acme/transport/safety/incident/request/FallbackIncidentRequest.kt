package com.acme.transport.safety.incident.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback incident request records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a fallback incident request is in its life cycle. */
enum class FallbackIncidentRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback incident request as the incident team keeps it. */
data class FallbackIncidentRequest(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var ownerId: String,
    var endsAt: Int,
    var version: Int,
    var weight: Double,
    var retries: Int,
    var notes: String,
    var status: FallbackIncidentRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIncidentRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIncidentRequestStatus): Boolean {
        when (status) {
            FallbackIncidentRequestStatus.CREATED -> return next == FallbackIncidentRequestStatus.RETIRED
            FallbackIncidentRequestStatus.VALIDATED -> {
                when (next) {
                    FallbackIncidentRequestStatus.RETIRED -> return true
                }
                return false
            }
            FallbackIncidentRequestStatus.PUBLISHED -> {
                when (next) {
                    FallbackIncidentRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackIncidentRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback incident request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round(version.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): FallbackIncidentRequest {
        return FallbackIncidentRequest(
            code = code,
            label = label,
            tags = tags,
            ownerId = ownerId,
            endsAt = endsAt,
            version = version,
            weight = weight,
            retries = retries,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback incident request in status created. */
fun newFallbackIncidentRequest(code: String, label: String): FallbackIncidentRequest {
    return FallbackIncidentRequest(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        ownerId = "",
        endsAt = 0,
        version = 0,
        weight = 0.0,
        retries = 0,
        notes = "",
        status = FallbackIncidentRequestStatus.CREATED,
    )
}
