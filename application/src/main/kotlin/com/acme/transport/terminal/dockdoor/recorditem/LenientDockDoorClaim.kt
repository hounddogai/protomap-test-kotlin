package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dock door claim is in its life cycle. */
enum class LenientDockDoorClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient dock door claim and its current status. */
data class LenientDockDoorClaim(
    var code: String,
    var name: String,
    var timezone: String,
    var segment: String,
    var loadKg: Double,
    var graceDays: Int,
    var status: LenientDockDoorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDockDoorClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDockDoorClaimStatus): Boolean {
        when (status) {
            LenientDockDoorClaimStatus.PLANNED -> {
                when (next) {
                    LenientDockDoorClaimStatus.IN_PROGRESS, LenientDockDoorClaimStatus.CANCELLED -> return true
                }
                return false
            }
            LenientDockDoorClaimStatus.SCHEDULED -> return next == LenientDockDoorClaimStatus.IN_PROGRESS
            LenientDockDoorClaimStatus.IN_PROGRESS -> {
                when (next) {
                    LenientDockDoorClaimStatus.COMPLETED -> return true
                }
                return false
            }
            LenientDockDoorClaimStatus.COMPLETED -> return next == LenientDockDoorClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientDockDoorClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient dock door claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): LenientDockDoorClaim {
        return LenientDockDoorClaim(
            code = code,
            name = name,
            timezone = timezone,
            segment = segment,
            loadKg = loadKg,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient dock door claim in status planned. */
fun newLenientDockDoorClaim(code: String, name: String): LenientDockDoorClaim {
    return LenientDockDoorClaim(
        code = code,
        name = name,
        timezone = "",
        segment = "",
        loadKg = 0.0,
        graceDays = 0,
        status = LenientDockDoorClaimStatus.PLANNED,
    )
}
