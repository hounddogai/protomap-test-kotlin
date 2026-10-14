package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dwell policy is in its life cycle. */
enum class LenientDwellPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient dwell policy. Its code is unique within a tenant. */
data class LenientDwellPolicy(
    var code: String,
    var name: String,
    var currency: String,
    var endsAt: Int,
    var loadKg: Double,
    var segment: String,
    var routeCode: String,
    var floorValue: Double,
    var country: String,
    var region: String,
    var startsAt: Int,
    var status: LenientDwellPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellPolicyStatus): Boolean {
        when (status) {
            LenientDwellPolicyStatus.REQUESTED -> {
                when (next) {
                    LenientDwellPolicyStatus.REVOKED, LenientDwellPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            LenientDwellPolicyStatus.GRANTED -> return next == LenientDwellPolicyStatus.EXPIRED
            LenientDwellPolicyStatus.REVOKED -> {
                when (next) {
                    LenientDwellPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDwellPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient dwell policy"} $code"

    fun adjustedExposure(): Double = round(endsAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): LenientDwellPolicy {
        return LenientDwellPolicy(
            code = code,
            name = name,
            currency = currency,
            endsAt = endsAt,
            loadKg = loadKg,
            segment = segment,
            routeCode = routeCode,
            floorValue = floorValue,
            country = country,
            region = region,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient dwell policy in status requested. */
fun newLenientDwellPolicy(code: String, name: String): LenientDwellPolicy {
    return LenientDwellPolicy(
        code = code,
        name = name,
        currency = "",
        endsAt = 0,
        loadKg = 0.0,
        segment = "",
        routeCode = "",
        floorValue = 0.0,
        country = "",
        region = "",
        startsAt = 0,
        status = LenientDwellPolicyStatus.REQUESTED,
    )
}
