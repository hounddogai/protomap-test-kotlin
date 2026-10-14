package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dock door tier is in its life cycle. */
enum class CappedDockDoorTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped dock door tier as the dock door team keeps it. */
data class CappedDockDoorTier(
    var code: String,
    var title: String,
    var approved: Boolean,
    var ceiling: Double,
    var createdAt: Int,
    var batchSize: Int,
    var region: String,
    var expiresAt: Int,
    var status: CappedDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorTierStatus): Boolean {
        when (status) {
            CappedDockDoorTierStatus.PROPOSED -> return next == CappedDockDoorTierStatus.APPROVED || next == CappedDockDoorTierStatus.REJECTED
            CappedDockDoorTierStatus.APPROVED -> {
                when (next) {
                    CappedDockDoorTierStatus.REJECTED, CappedDockDoorTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedDockDoorTierStatus.REJECTED -> return next == CappedDockDoorTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped dock door tier"} $code"

    fun weightedExposure(): Double = round((ceiling - createdAt.toDouble()) * 100) / 100

    fun snapshot(): CappedDockDoorTier {
        return CappedDockDoorTier(
            code = code,
            title = title,
            approved = approved,
            ceiling = ceiling,
            createdAt = createdAt,
            batchSize = batchSize,
            region = region,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped dock door tier in status proposed. */
fun newCappedDockDoorTier(code: String, title: String): CappedDockDoorTier {
    return CappedDockDoorTier(
        code = code,
        title = title,
        approved = false,
        ceiling = 0.0,
        createdAt = 0,
        batchSize = 0,
        region = "",
        expiresAt = 0,
        status = CappedDockDoorTierStatus.PROPOSED,
    )
}
