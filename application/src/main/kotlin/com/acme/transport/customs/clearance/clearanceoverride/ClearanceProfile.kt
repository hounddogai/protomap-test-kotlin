package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance profile is in its life cycle. */
enum class ClearanceProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A clearance profile as the clearance team keeps it. */
data class ClearanceProfile(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var channel: String,
    var windowDays: Int,
    var odometer: Int,
    var amount: Double,
    var driverId: String,
    var revision: Int,
    var tags: MutableList<String>,
    var status: ClearanceProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceProfileStatus): Boolean {
        when (status) {
            ClearanceProfileStatus.CREATED -> {
                when (next) {
                    ClearanceProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            ClearanceProfileStatus.VALIDATED -> {
                when (next) {
                    ClearanceProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            ClearanceProfileStatus.PUBLISHED -> {
                when (next) {
                    ClearanceProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceProfileStatus) {
        if (!canMoveTo(next)) {
            error("clearance profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((revision.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ClearanceProfile {
        return ClearanceProfile(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            channel = channel,
            windowDays = windowDays,
            odometer = odometer,
            amount = amount,
            driverId = driverId,
            revision = revision,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a clearance profile in status created. */
fun newClearanceProfile(code: String, label: String): ClearanceProfile {
    return ClearanceProfile(
        code = code,
        label = label,
        durationMinutes = 0,
        channel = "",
        windowDays = 0,
        odometer = 0,
        amount = 0.0,
        driverId = "",
        revision = 0,
        tags = mutableListOf<String>(),
        status = ClearanceProfileStatus.CREATED,
    )
}
