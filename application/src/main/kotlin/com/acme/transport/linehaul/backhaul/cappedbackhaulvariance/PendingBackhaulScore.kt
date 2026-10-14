package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending backhaul score is in its life cycle. */
enum class PendingBackhaulScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending backhaul score. Its code is unique within a tenant. */
data class PendingBackhaulScore(
    var code: String,
    var title: String,
    var startsAt: Int,
    var attempts: Int,
    var active: Boolean,
    var fuelLiters: Double,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var priority: Int,
    var distanceKm: Double,
    var status: PendingBackhaulScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBackhaulScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBackhaulScoreStatus): Boolean {
        when (status) {
            PendingBackhaulScoreStatus.REQUESTED -> return next == PendingBackhaulScoreStatus.GRANTED
            PendingBackhaulScoreStatus.GRANTED -> {
                when (next) {
                    PendingBackhaulScoreStatus.EXPIRED -> return true
                }
                return false
            }
            PendingBackhaulScoreStatus.REVOKED -> return next == PendingBackhaulScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingBackhaulScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending backhaul score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending backhaul score"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(attempts.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): PendingBackhaulScore {
        return PendingBackhaulScore(
            code = code,
            title = title,
            startsAt = startsAt,
            attempts = attempts,
            active = active,
            fuelLiters = fuelLiters,
            attributes = attributes,
            capacity = capacity,
            priority = priority,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending backhaul score in status requested. */
fun newPendingBackhaulScore(code: String, title: String): PendingBackhaulScore {
    return PendingBackhaulScore(
        code = code,
        title = title,
        startsAt = 0,
        attempts = 0,
        active = false,
        fuelLiters = 0.0,
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        priority = 0,
        distanceKm = 0.0,
        status = PendingBackhaulScoreStatus.REQUESTED,
    )
}
