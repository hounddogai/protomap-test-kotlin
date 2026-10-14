package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary tire request records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a primary tire request is in its life cycle. */
enum class PrimaryTireRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary tire request. Its code is unique within a tenant. */
data class PrimaryTireRequest(
    var code: String,
    var title: String,
    var category: String,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var stopCount: Int,
    var trailerId: String,
    var country: String,
    var timezone: String,
    var status: PrimaryTireRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTireRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTireRequestStatus): Boolean {
        when (status) {
            PrimaryTireRequestStatus.PROPOSED -> {
                when (next) {
                    PrimaryTireRequestStatus.REJECTED, PrimaryTireRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTireRequestStatus.APPROVED -> return next == PrimaryTireRequestStatus.REJECTED || next == PrimaryTireRequestStatus.WITHDRAWN
            PrimaryTireRequestStatus.REJECTED -> {
                when (next) {
                    PrimaryTireRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTireRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary tire request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(loadKg * budget * 100) / 100

    fun snapshot(): PrimaryTireRequest {
        return PrimaryTireRequest(
            code = code,
            title = title,
            category = category,
            budget = budget,
            attributes = attributes,
            loadKg = loadKg,
            stopCount = stopCount,
            trailerId = trailerId,
            country = country,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary tire request in status proposed. */
fun newPrimaryTireRequest(code: String, title: String): PrimaryTireRequest {
    return PrimaryTireRequest(
        code = code,
        title = title,
        category = "",
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        stopCount = 0,
        trailerId = "",
        country = "",
        timezone = "",
        status = PrimaryTireRequestStatus.PROPOSED,
    )
}
