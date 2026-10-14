package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped relay claim is in its life cycle. */
enum class CappedRelayClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped relay claim. Its code is unique within a tenant. */
data class CappedRelayClaim(
    var code: String,
    var name: String,
    var lane: String,
    var windowDays: Int,
    var endsAt: Int,
    var durationMinutes: Int,
    var graceDays: Int,
    var depot: String,
    var shiftCode: String,
    var status: CappedRelayClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRelayClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRelayClaimStatus): Boolean {
        when (status) {
            CappedRelayClaimStatus.DRAFT -> {
                when (next) {
                    CappedRelayClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedRelayClaimStatus.PENDING -> return next == CappedRelayClaimStatus.ACTIVE || next == CappedRelayClaimStatus.CLOSED
            CappedRelayClaimStatus.ACTIVE -> {
                when (next) {
                    CappedRelayClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedRelayClaimStatus.SUSPENDED -> {
                when (next) {
                    CappedRelayClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRelayClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped relay claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped relay claim"} $code"

    fun effectiveExposure(): Double = round(durationMinutes.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): CappedRelayClaim {
        return CappedRelayClaim(
            code = code,
            name = name,
            lane = lane,
            windowDays = windowDays,
            endsAt = endsAt,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            depot = depot,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped relay claim in status draft. */
fun newCappedRelayClaim(code: String, name: String): CappedRelayClaim {
    return CappedRelayClaim(
        code = code,
        name = name,
        lane = "",
        windowDays = 0,
        endsAt = 0,
        durationMinutes = 0,
        graceDays = 0,
        depot = "",
        shiftCode = "",
        status = CappedRelayClaimStatus.DRAFT,
    )
}
