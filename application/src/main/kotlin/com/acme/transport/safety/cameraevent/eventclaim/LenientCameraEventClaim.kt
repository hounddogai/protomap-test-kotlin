package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient camera event claim records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a lenient camera event claim is in its life cycle. */
enum class LenientCameraEventClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient camera event claim. Its code is unique within a tenant. */
data class LenientCameraEventClaim(
    var code: String,
    var title: String,
    var limit: Int,
    var version: Int,
    var country: String,
    var startsAt: Int,
    var shiftCode: String,
    var attempts: Int,
    var category: String,
    var description: String,
    var unit: String,
    var status: LenientCameraEventClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCameraEventClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCameraEventClaimStatus): Boolean {
        when (status) {
            LenientCameraEventClaimStatus.NEW -> {
                when (next) {
                    LenientCameraEventClaimStatus.QUEUED, LenientCameraEventClaimStatus.DONE -> return true
                }
                return false
            }
            LenientCameraEventClaimStatus.QUEUED -> {
                when (next) {
                    LenientCameraEventClaimStatus.RUNNING -> return true
                }
                return false
            }
            LenientCameraEventClaimStatus.RUNNING -> return next == LenientCameraEventClaimStatus.DONE || next == LenientCameraEventClaimStatus.FAILED
            LenientCameraEventClaimStatus.DONE -> return next == LenientCameraEventClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientCameraEventClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient camera event claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"lenient camera event claim"} $code"

    fun weightedLoad(): Double = round((startsAt.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): LenientCameraEventClaim {
        return LenientCameraEventClaim(
            code = code,
            title = title,
            limit = limit,
            version = version,
            country = country,
            startsAt = startsAt,
            shiftCode = shiftCode,
            attempts = attempts,
            category = category,
            description = description,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient camera event claim in status new. */
fun newLenientCameraEventClaim(code: String, title: String): LenientCameraEventClaim {
    return LenientCameraEventClaim(
        code = code,
        title = title,
        limit = 0,
        version = 0,
        country = "",
        startsAt = 0,
        shiftCode = "",
        attempts = 0,
        category = "",
        description = "",
        unit = "",
        status = LenientCameraEventClaimStatus.NEW,
    )
}
