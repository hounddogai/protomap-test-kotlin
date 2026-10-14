package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone claim is in its life cycle. */
enum class ZoneClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a zone claim and its current status. */
data class ZoneClaim(
    var code: String,
    var label: String,
    var limit: Int,
    var timezone: String,
    var depot: String,
    var retries: Int,
    var approved: Boolean,
    var category: String,
    var status: ZoneClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneClaimStatus): Boolean {
        when (status) {
            ZoneClaimStatus.DRAFT -> {
                when (next) {
                    ZoneClaimStatus.CLOSED -> return true
                }
                return false
            }
            ZoneClaimStatus.PENDING -> return next == ZoneClaimStatus.SUSPENDED
            ZoneClaimStatus.ACTIVE -> return next == ZoneClaimStatus.SUSPENDED
            ZoneClaimStatus.SUSPENDED -> {
                when (next) {
                    ZoneClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneClaimStatus) {
        if (!canMoveTo(next)) {
            error("zone claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((limit.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): ZoneClaim {
        return ZoneClaim(
            code = code,
            label = label,
            limit = limit,
            timezone = timezone,
            depot = depot,
            retries = retries,
            approved = approved,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a zone claim in status draft. */
fun newZoneClaim(code: String, label: String): ZoneClaim {
    return ZoneClaim(
        code = code,
        label = label,
        limit = 0,
        timezone = "",
        depot = "",
        retries = 0,
        approved = false,
        category = "",
        status = ZoneClaimStatus.DRAFT,
    )
}
