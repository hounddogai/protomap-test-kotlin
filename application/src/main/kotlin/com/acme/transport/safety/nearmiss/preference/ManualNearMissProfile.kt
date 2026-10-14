package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual near miss profile is in its life cycle. */
enum class ManualNearMissProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One manual near miss profile, with what the planners need to act on it. */
data class ManualNearMissProfile(
    var code: String,
    var name: String,
    var checksum: String,
    var externalRef: String,
    var unit: String,
    var updatedAt: Int,
    var archived: Boolean,
    var loadKg: Double,
    var status: ManualNearMissProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualNearMissProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualNearMissProfileStatus): Boolean {
        when (status) {
            ManualNearMissProfileStatus.IDLE -> return next == ManualNearMissProfileStatus.READY || next == ManualNearMissProfileStatus.STOPPED
            ManualNearMissProfileStatus.WARMING -> {
                when (next) {
                    ManualNearMissProfileStatus.DRAINING, ManualNearMissProfileStatus.STOPPED -> return true
                }
                return false
            }
            ManualNearMissProfileStatus.READY -> {
                when (next) {
                    ManualNearMissProfileStatus.DRAINING -> return true
                }
                return false
            }
            ManualNearMissProfileStatus.DRAINING -> return next == ManualNearMissProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualNearMissProfileStatus) {
        if (!canMoveTo(next)) {
            error("manual near miss profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((updatedAt.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ManualNearMissProfile {
        return ManualNearMissProfile(
            code = code,
            name = name,
            checksum = checksum,
            externalRef = externalRef,
            unit = unit,
            updatedAt = updatedAt,
            archived = archived,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual near miss profile in status idle. */
fun newManualNearMissProfile(code: String, name: String): ManualNearMissProfile {
    return ManualNearMissProfile(
        code = code,
        name = name,
        checksum = "",
        externalRef = "",
        unit = "",
        updatedAt = 0,
        archived = false,
        loadKg = 0.0,
        status = ManualNearMissProfileStatus.IDLE,
    )
}
