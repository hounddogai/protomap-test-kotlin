package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery profile is in its life cycle. */
enum class RedeliveryProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a redelivery profile and its current status. */
data class RedeliveryProfile(
    var code: String,
    var name: String,
    var routeCode: String,
    var budget: Double,
    var checksum: String,
    var region: String,
    var shiftCode: String,
    var floorValue: Double,
    var retries: Int,
    var score: Double,
    var status: RedeliveryProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryProfileStatus): Boolean {
        when (status) {
            RedeliveryProfileStatus.OPEN -> return next == RedeliveryProfileStatus.IN_REVIEW || next == RedeliveryProfileStatus.ARCHIVED
            RedeliveryProfileStatus.IN_REVIEW -> {
                when (next) {
                    RedeliveryProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            RedeliveryProfileStatus.RESOLVED -> return next == RedeliveryProfileStatus.REOPENED || next == RedeliveryProfileStatus.ARCHIVED
            RedeliveryProfileStatus.REOPENED -> {
                when (next) {
                    RedeliveryProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryProfileStatus) {
        if (!canMoveTo(next)) {
            error("redelivery profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round((retries.toDouble() + score) * 100) / 100

    fun snapshot(): RedeliveryProfile {
        return RedeliveryProfile(
            code = code,
            name = name,
            routeCode = routeCode,
            budget = budget,
            checksum = checksum,
            region = region,
            shiftCode = shiftCode,
            floorValue = floorValue,
            retries = retries,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a redelivery profile in status open. */
fun newRedeliveryProfile(code: String, name: String): RedeliveryProfile {
    return RedeliveryProfile(
        code = code,
        name = name,
        routeCode = "",
        budget = 0.0,
        checksum = "",
        region = "",
        shiftCode = "",
        floorValue = 0.0,
        retries = 0,
        score = 0.0,
        status = RedeliveryProfileStatus.OPEN,
    )
}
