package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched defect profile is in its life cycle. */
enum class BatchedDefectProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched defect profile and its current status. */
data class BatchedDefectProfile(
    var code: String,
    var name: String,
    var stopCount: Int,
    var country: String,
    var attempts: Int,
    var externalRef: String,
    var loadKg: Double,
    var etaMinutes: Int,
    var notes: String,
    var windowDays: Int,
    var startsAt: Int,
    var status: BatchedDefectProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDefectProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDefectProfileStatus): Boolean {
        when (status) {
            BatchedDefectProfileStatus.REQUESTED -> return next == BatchedDefectProfileStatus.GRANTED
            BatchedDefectProfileStatus.GRANTED -> return next == BatchedDefectProfileStatus.EXPIRED
            BatchedDefectProfileStatus.REVOKED -> return next == BatchedDefectProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedDefectProfileStatus) {
        if (!canMoveTo(next)) {
            error("batched defect profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((loadKg + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): BatchedDefectProfile {
        return BatchedDefectProfile(
            code = code,
            name = name,
            stopCount = stopCount,
            country = country,
            attempts = attempts,
            externalRef = externalRef,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            notes = notes,
            windowDays = windowDays,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched defect profile in status requested. */
fun newBatchedDefectProfile(code: String, name: String): BatchedDefectProfile {
    return BatchedDefectProfile(
        code = code,
        name = name,
        stopCount = 0,
        country = "",
        attempts = 0,
        externalRef = "",
        loadKg = 0.0,
        etaMinutes = 0,
        notes = "",
        windowDays = 0,
        startsAt = 0,
        status = BatchedDefectProfileStatus.REQUESTED,
    )
}
