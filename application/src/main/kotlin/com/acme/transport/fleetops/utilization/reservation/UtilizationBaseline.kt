package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization baseline is in its life cycle. */
enum class UtilizationBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A utilization baseline as the utilization team keeps it. */
data class UtilizationBaseline(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var approved: Boolean,
    var phone: String,
    var pallets: Int,
    var status: UtilizationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationBaselineStatus): Boolean {
        when (status) {
            UtilizationBaselineStatus.REQUESTED -> return next == UtilizationBaselineStatus.GRANTED
            UtilizationBaselineStatus.GRANTED -> return next == UtilizationBaselineStatus.EXPIRED
            UtilizationBaselineStatus.REVOKED -> {
                when (next) {
                    UtilizationBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("utilization baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"utilization baseline"} $code"

    fun weightedVolume(): Double = round((pallets.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): UtilizationBaseline {
        return UtilizationBaseline(
            code = code,
            name = name,
            expiresAt = expiresAt,
            approved = approved,
            phone = phone,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a utilization baseline in status requested. */
fun newUtilizationBaseline(code: String, name: String): UtilizationBaseline {
    return UtilizationBaseline(
        code = code,
        name = name,
        expiresAt = 0,
        approved = false,
        phone = "",
        pallets = 0,
        status = UtilizationBaselineStatus.REQUESTED,
    )
}
