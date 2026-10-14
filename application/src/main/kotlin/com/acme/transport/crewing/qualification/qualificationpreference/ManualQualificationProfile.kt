package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual qualification profile is in its life cycle. */
enum class ManualQualificationProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual qualification profile, with what the planners need to act on it. */
data class ManualQualificationProfile(
    var code: String,
    var title: String,
    var startsAt: Int,
    var label: String,
    var active: Boolean,
    var notes: String,
    var routeCode: String,
    var dock: String,
    var vehicleId: String,
    var fuelLiters: Double,
    var stopCount: Int,
    var status: ManualQualificationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualQualificationProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualQualificationProfileStatus): Boolean {
        when (status) {
            ManualQualificationProfileStatus.PROPOSED -> return next == ManualQualificationProfileStatus.REJECTED
            ManualQualificationProfileStatus.APPROVED -> {
                when (next) {
                    ManualQualificationProfileStatus.REJECTED, ManualQualificationProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualQualificationProfileStatus.REJECTED -> {
                when (next) {
                    ManualQualificationProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualQualificationProfileStatus) {
        if (!canMoveTo(next)) {
            error("manual qualification profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun weightedLoad(): Double = round((stopCount.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ManualQualificationProfile {
        return ManualQualificationProfile(
            code = code,
            title = title,
            startsAt = startsAt,
            label = label,
            active = active,
            notes = notes,
            routeCode = routeCode,
            dock = dock,
            vehicleId = vehicleId,
            fuelLiters = fuelLiters,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual qualification profile in status proposed. */
fun newManualQualificationProfile(code: String, title: String): ManualQualificationProfile {
    return ManualQualificationProfile(
        code = code,
        title = title,
        startsAt = 0,
        label = "",
        active = false,
        notes = "",
        routeCode = "",
        dock = "",
        vehicleId = "",
        fuelLiters = 0.0,
        stopCount = 0,
        status = ManualQualificationProfileStatus.PROPOSED,
    )
}
