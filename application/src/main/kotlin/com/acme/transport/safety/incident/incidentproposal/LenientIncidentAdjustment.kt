package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient incident adjustment is in its life cycle. */
enum class LenientIncidentAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient incident adjustment as the incident team keeps it. */
data class LenientIncidentAdjustment(
    var code: String,
    var name: String,
    var limit: Int,
    var windowDays: Int,
    var retries: Int,
    var pinned: Boolean,
    var floorValue: Double,
    var expiresAt: Int,
    var weight: Double,
    var status: LenientIncidentAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIncidentAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIncidentAdjustmentStatus): Boolean {
        when (status) {
            LenientIncidentAdjustmentStatus.NEW -> return next == LenientIncidentAdjustmentStatus.RUNNING || next == LenientIncidentAdjustmentStatus.DONE
            LenientIncidentAdjustmentStatus.QUEUED -> return next == LenientIncidentAdjustmentStatus.RUNNING || next == LenientIncidentAdjustmentStatus.DONE
            LenientIncidentAdjustmentStatus.RUNNING -> return next == LenientIncidentAdjustmentStatus.DONE || next == LenientIncidentAdjustmentStatus.FAILED
            LenientIncidentAdjustmentStatus.DONE -> return next == LenientIncidentAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientIncidentAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient incident adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient incident adjustment"} $code"

    fun weightedValue(): Double = round(windowDays.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): LenientIncidentAdjustment {
        return LenientIncidentAdjustment(
            code = code,
            name = name,
            limit = limit,
            windowDays = windowDays,
            retries = retries,
            pinned = pinned,
            floorValue = floorValue,
            expiresAt = expiresAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient incident adjustment in status new. */
fun newLenientIncidentAdjustment(code: String, name: String): LenientIncidentAdjustment {
    return LenientIncidentAdjustment(
        code = code,
        name = name,
        limit = 0,
        windowDays = 0,
        retries = 0,
        pinned = false,
        floorValue = 0.0,
        expiresAt = 0,
        weight = 0.0,
        status = LenientIncidentAdjustmentStatus.NEW,
    )
}
