package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling claim is in its life cycle. */
enum class IdlingClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling claim. Its code is unique within a tenant. */
data class IdlingClaim(
    var code: String,
    var title: String,
    var phone: String,
    var ceiling: Double,
    var ownerId: String,
    var stopCount: Int,
    var lane: String,
    var source: String,
    var ratio: Double,
    var durationMinutes: Int,
    var status: IdlingClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingClaimStatus): Boolean {
        when (status) {
            IdlingClaimStatus.CREATED -> return next == IdlingClaimStatus.VALIDATED || next == IdlingClaimStatus.PUBLISHED
            IdlingClaimStatus.VALIDATED -> {
                when (next) {
                    IdlingClaimStatus.PUBLISHED, IdlingClaimStatus.RETIRED -> return true
                }
                return false
            }
            IdlingClaimStatus.PUBLISHED -> {
                when (next) {
                    IdlingClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingClaimStatus) {
        if (!canMoveTo(next)) {
            error("idling claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round(stopCount.toDouble() * ratio * 100) / 100

    fun snapshot(): IdlingClaim {
        return IdlingClaim(
            code = code,
            title = title,
            phone = phone,
            ceiling = ceiling,
            ownerId = ownerId,
            stopCount = stopCount,
            lane = lane,
            source = source,
            ratio = ratio,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a idling claim in status created. */
fun newIdlingClaim(code: String, title: String): IdlingClaim {
    return IdlingClaim(
        code = code,
        title = title,
        phone = "",
        ceiling = 0.0,
        ownerId = "",
        stopCount = 0,
        lane = "",
        source = "",
        ratio = 0.0,
        durationMinutes = 0,
        status = IdlingClaimStatus.CREATED,
    )
}
