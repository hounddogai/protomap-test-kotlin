package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tire rollup is in its life cycle. */
enum class FallbackTireRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback tire rollup. Its code is unique within a tenant. */
data class FallbackTireRollup(
    var code: String,
    var title: String,
    var limit: Int,
    var archived: Boolean,
    var source: String,
    var margin: Double,
    var depot: String,
    var trailerId: String,
    var revision: Int,
    var approved: Boolean,
    var status: FallbackTireRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTireRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTireRollupStatus): Boolean {
        when (status) {
            FallbackTireRollupStatus.PROPOSED -> return next == FallbackTireRollupStatus.APPROVED || next == FallbackTireRollupStatus.WITHDRAWN
            FallbackTireRollupStatus.APPROVED -> return next == FallbackTireRollupStatus.REJECTED
            FallbackTireRollupStatus.REJECTED -> return next == FallbackTireRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackTireRollupStatus) {
        if (!canMoveTo(next)) {
            error("fallback tire rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"fallback tire rollup"} $code"

    fun weightedTotal(): Double = round(margin * revision.toDouble() * 100) / 100

    fun snapshot(): FallbackTireRollup {
        return FallbackTireRollup(
            code = code,
            title = title,
            limit = limit,
            archived = archived,
            source = source,
            margin = margin,
            depot = depot,
            trailerId = trailerId,
            revision = revision,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback tire rollup in status proposed. */
fun newFallbackTireRollup(code: String, title: String): FallbackTireRollup {
    return FallbackTireRollup(
        code = code,
        title = title,
        limit = 0,
        archived = false,
        source = "",
        margin = 0.0,
        depot = "",
        trailerId = "",
        revision = 0,
        approved = false,
        status = FallbackTireRollupStatus.PROPOSED,
    )
}
