package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss profile records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss profile is in its life cycle. */
enum class NearMissProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a near miss profile and its current status. */
data class NearMissProfile(
    var code: String,
    var name: String,
    var floorValue: Double,
    var stopCount: Int,
    var region: String,
    var label: String,
    var visible: Boolean,
    var active: Boolean,
    var status: NearMissProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissProfileStatus): Boolean {
        when (status) {
            NearMissProfileStatus.DRAFT -> return next == NearMissProfileStatus.SUSPENDED
            NearMissProfileStatus.PENDING -> {
                when (next) {
                    NearMissProfileStatus.ACTIVE -> return true
                }
                return false
            }
            NearMissProfileStatus.ACTIVE -> return next == NearMissProfileStatus.SUSPENDED
            NearMissProfileStatus.SUSPENDED -> return next == NearMissProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NearMissProfileStatus) {
        if (!canMoveTo(next)) {
            error("near miss profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun netExposure(): Double = round((stopCount.toDouble() + floorValue) * 100) / 100

    fun snapshot(): NearMissProfile {
        return NearMissProfile(
            code = code,
            name = name,
            floorValue = floorValue,
            stopCount = stopCount,
            region = region,
            label = label,
            visible = visible,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a near miss profile in status draft. */
fun newNearMissProfile(code: String, name: String): NearMissProfile {
    return NearMissProfile(
        code = code,
        name = name,
        floorValue = 0.0,
        stopCount = 0,
        region = "",
        label = "",
        visible = false,
        active = false,
        status = NearMissProfileStatus.DRAFT,
    )
}
