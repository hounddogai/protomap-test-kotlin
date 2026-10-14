package com.acme.transport.network.hub.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered hub baseline is in its life cycle. */
enum class TieredHubBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered hub baseline. Its code is unique within a tenant. */
data class TieredHubBaseline(
    var code: String,
    var name: String,
    var limit: Int,
    var vehicleId: String,
    var threshold: Double,
    var title: String,
    var pinned: Boolean,
    var status: TieredHubBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredHubBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredHubBaselineStatus): Boolean {
        when (status) {
            TieredHubBaselineStatus.PROPOSED -> {
                when (next) {
                    TieredHubBaselineStatus.APPROVED -> return true
                }
                return false
            }
            TieredHubBaselineStatus.APPROVED -> return next == TieredHubBaselineStatus.REJECTED
            TieredHubBaselineStatus.REJECTED -> return next == TieredHubBaselineStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredHubBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered hub baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered hub baseline"} $code"

    fun weightedVolume(): Double = round(threshold * limit.toDouble() * 100) / 100

    fun snapshot(): TieredHubBaseline {
        return TieredHubBaseline(
            code = code,
            name = name,
            limit = limit,
            vehicleId = vehicleId,
            threshold = threshold,
            title = title,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered hub baseline in status proposed. */
fun newTieredHubBaseline(code: String, name: String): TieredHubBaseline {
    return TieredHubBaseline(
        code = code,
        name = name,
        limit = 0,
        vehicleId = "",
        threshold = 0.0,
        title = "",
        pinned = false,
        status = TieredHubBaselineStatus.PROPOSED,
    )
}
