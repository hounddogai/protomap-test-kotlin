package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan profile is in its life cycle. */
enum class LanePlanProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lane plan profile. Its code is unique within a tenant. */
data class LanePlanProfile(
    var code: String,
    var label: String,
    var trailerId: String,
    var floorValue: Double,
    var sequence: Int,
    var distanceKm: Double,
    var dock: String,
    var cost: Double,
    var status: LanePlanProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanProfileStatus): Boolean {
        when (status) {
            LanePlanProfileStatus.REQUESTED -> {
                when (next) {
                    LanePlanProfileStatus.GRANTED -> return true
                }
                return false
            }
            LanePlanProfileStatus.GRANTED -> return next == LanePlanProfileStatus.REVOKED || next == LanePlanProfileStatus.EXPIRED
            LanePlanProfileStatus.REVOKED -> return next == LanePlanProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LanePlanProfileStatus) {
        if (!canMoveTo(next)) {
            error("lane plan profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round(distanceKm * floorValue * 100) / 100

    fun snapshot(): LanePlanProfile {
        return LanePlanProfile(
            code = code,
            label = label,
            trailerId = trailerId,
            floorValue = floorValue,
            sequence = sequence,
            distanceKm = distanceKm,
            dock = dock,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lane plan profile in status requested. */
fun newLanePlanProfile(code: String, label: String): LanePlanProfile {
    return LanePlanProfile(
        code = code,
        label = label,
        trailerId = "",
        floorValue = 0.0,
        sequence = 0,
        distanceKm = 0.0,
        dock = "",
        cost = 0.0,
        status = LanePlanProfileStatus.REQUESTED,
    )
}
