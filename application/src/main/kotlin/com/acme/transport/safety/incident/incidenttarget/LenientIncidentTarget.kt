package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient incident target records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a lenient incident target is in its life cycle. */
enum class LenientIncidentTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient incident target as the incident team keeps it. */
data class LenientIncidentTarget(
    var code: String,
    var title: String,
    var batchSize: Int,
    var pinned: Boolean,
    var lane: String,
    var driverId: String,
    var shiftCode: String,
    var country: String,
    var minimum: Double,
    var createdAt: Int,
    var windowDays: Int,
    var status: LenientIncidentTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIncidentTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIncidentTargetStatus): Boolean {
        when (status) {
            LenientIncidentTargetStatus.DRAFT -> {
                when (next) {
                    LenientIncidentTargetStatus.ACTIVE -> return true
                }
                return false
            }
            LenientIncidentTargetStatus.PENDING -> return next == LenientIncidentTargetStatus.ACTIVE || next == LenientIncidentTargetStatus.CLOSED
            LenientIncidentTargetStatus.ACTIVE -> return next == LenientIncidentTargetStatus.SUSPENDED || next == LenientIncidentTargetStatus.CLOSED
            LenientIncidentTargetStatus.SUSPENDED -> {
                when (next) {
                    LenientIncidentTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientIncidentTargetStatus) {
        if (!canMoveTo(next)) {
            error("lenient incident target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((batchSize.toDouble() + minimum) * 100) / 100

    fun snapshot(): LenientIncidentTarget {
        return LenientIncidentTarget(
            code = code,
            title = title,
            batchSize = batchSize,
            pinned = pinned,
            lane = lane,
            driverId = driverId,
            shiftCode = shiftCode,
            country = country,
            minimum = minimum,
            createdAt = createdAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient incident target in status draft. */
fun newLenientIncidentTarget(code: String, title: String): LenientIncidentTarget {
    return LenientIncidentTarget(
        code = code,
        title = title,
        batchSize = 0,
        pinned = false,
        lane = "",
        driverId = "",
        shiftCode = "",
        country = "",
        minimum = 0.0,
        createdAt = 0,
        windowDays = 0,
        status = LenientIncidentTargetStatus.DRAFT,
    )
}
