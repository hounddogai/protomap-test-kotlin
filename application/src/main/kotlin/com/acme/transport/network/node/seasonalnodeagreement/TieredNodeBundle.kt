package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered node bundle is in its life cycle. */
enum class TieredNodeBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered node bundle as the node team keeps it. */
data class TieredNodeBundle(
    var code: String,
    var name: String,
    var rate: Double,
    var expiresAt: Int,
    var lane: String,
    var segment: String,
    var score: Double,
    var maximum: Double,
    var status: TieredNodeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNodeBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNodeBundleStatus): Boolean {
        when (status) {
            TieredNodeBundleStatus.OPEN -> return next == TieredNodeBundleStatus.ARCHIVED
            TieredNodeBundleStatus.IN_REVIEW -> {
                when (next) {
                    TieredNodeBundleStatus.REOPENED, TieredNodeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredNodeBundleStatus.RESOLVED -> {
                when (next) {
                    TieredNodeBundleStatus.REOPENED, TieredNodeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredNodeBundleStatus.REOPENED -> {
                when (next) {
                    TieredNodeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredNodeBundleStatus) {
        if (!canMoveTo(next)) {
            error("tiered node bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((expiresAt.toDouble() - rate) * 100) / 100

    fun snapshot(): TieredNodeBundle {
        return TieredNodeBundle(
            code = code,
            name = name,
            rate = rate,
            expiresAt = expiresAt,
            lane = lane,
            segment = segment,
            score = score,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered node bundle in status open. */
fun newTieredNodeBundle(code: String, name: String): TieredNodeBundle {
    return TieredNodeBundle(
        code = code,
        name = name,
        rate = 0.0,
        expiresAt = 0,
        lane = "",
        segment = "",
        score = 0.0,
        maximum = 0.0,
        status = TieredNodeBundleStatus.OPEN,
    )
}
