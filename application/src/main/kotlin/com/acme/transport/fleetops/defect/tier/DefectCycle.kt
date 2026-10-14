package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect cycle is in its life cycle. */
enum class DefectCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A defect cycle as the defect team keeps it. */
data class DefectCycle(
    var code: String,
    var label: String,
    var startsAt: Int,
    var fuelLiters: Double,
    var retries: Int,
    var expiresAt: Int,
    var title: String,
    var etaMinutes: Int,
    var distanceKm: Double,
    var externalRef: String,
    var approved: Boolean,
    var status: DefectCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectCycleStatus): Boolean {
        when (status) {
            DefectCycleStatus.REQUESTED -> return next == DefectCycleStatus.REVOKED || next == DefectCycleStatus.EXPIRED
            DefectCycleStatus.GRANTED -> return next == DefectCycleStatus.REVOKED || next == DefectCycleStatus.EXPIRED
            DefectCycleStatus.REVOKED -> return next == DefectCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefectCycleStatus) {
        if (!canMoveTo(next)) {
            error("defect cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"defect cycle"} $code"

    fun weightedExposure(): Double = round((distanceKm - retries.toDouble()) * 100) / 100

    fun snapshot(): DefectCycle {
        return DefectCycle(
            code = code,
            label = label,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            retries = retries,
            expiresAt = expiresAt,
            title = title,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            externalRef = externalRef,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect cycle in status requested. */
fun newDefectCycle(code: String, label: String): DefectCycle {
    return DefectCycle(
        code = code,
        label = label,
        startsAt = 0,
        fuelLiters = 0.0,
        retries = 0,
        expiresAt = 0,
        title = "",
        etaMinutes = 0,
        distanceKm = 0.0,
        externalRef = "",
        approved = false,
        status = DefectCycleStatus.REQUESTED,
    )
}
