package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient backhaul request is in its life cycle. */
enum class LenientBackhaulRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient backhaul request as the backhaul team keeps it. */
data class LenientBackhaulRequest(
    var code: String,
    var name: String,
    var visible: Boolean,
    var driverId: String,
    var dock: String,
    var country: String,
    var durationMinutes: Int,
    var minimum: Double,
    var trailerId: String,
    var score: Double,
    var status: LenientBackhaulRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBackhaulRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBackhaulRequestStatus): Boolean {
        when (status) {
            LenientBackhaulRequestStatus.PROPOSED -> {
                when (next) {
                    LenientBackhaulRequestStatus.REJECTED, LenientBackhaulRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientBackhaulRequestStatus.APPROVED -> return next == LenientBackhaulRequestStatus.REJECTED || next == LenientBackhaulRequestStatus.WITHDRAWN
            LenientBackhaulRequestStatus.REJECTED -> {
                when (next) {
                    LenientBackhaulRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientBackhaulRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient backhaul request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((score + minimum) * 100) / 100

    fun snapshot(): LenientBackhaulRequest {
        return LenientBackhaulRequest(
            code = code,
            name = name,
            visible = visible,
            driverId = driverId,
            dock = dock,
            country = country,
            durationMinutes = durationMinutes,
            minimum = minimum,
            trailerId = trailerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient backhaul request in status proposed. */
fun newLenientBackhaulRequest(code: String, name: String): LenientBackhaulRequest {
    return LenientBackhaulRequest(
        code = code,
        name = name,
        visible = false,
        driverId = "",
        dock = "",
        country = "",
        durationMinutes = 0,
        minimum = 0.0,
        trailerId = "",
        score = 0.0,
        status = LenientBackhaulRequestStatus.PROPOSED,
    )
}
