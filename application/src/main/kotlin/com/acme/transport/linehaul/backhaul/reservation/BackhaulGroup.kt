package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul group is in its life cycle. */
enum class BackhaulGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a backhaul group and its current status. */
data class BackhaulGroup(
    var code: String,
    var title: String,
    var stopCount: Int,
    var pinned: Boolean,
    var quantity: Int,
    var margin: Double,
    var score: Double,
    var depot: String,
    var expiresAt: Int,
    var status: BackhaulGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulGroupStatus): Boolean {
        when (status) {
            BackhaulGroupStatus.PROPOSED -> {
                when (next) {
                    BackhaulGroupStatus.APPROVED, BackhaulGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
            BackhaulGroupStatus.APPROVED -> return next == BackhaulGroupStatus.REJECTED || next == BackhaulGroupStatus.WITHDRAWN
            BackhaulGroupStatus.REJECTED -> return next == BackhaulGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BackhaulGroupStatus) {
        if (!canMoveTo(next)) {
            error("backhaul group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"backhaul group"} $code"

    fun weightedTotal(): Double = round(quantity.toDouble() * score * 100) / 100

    fun snapshot(): BackhaulGroup {
        return BackhaulGroup(
            code = code,
            title = title,
            stopCount = stopCount,
            pinned = pinned,
            quantity = quantity,
            margin = margin,
            score = score,
            depot = depot,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a backhaul group in status proposed. */
fun newBackhaulGroup(code: String, title: String): BackhaulGroup {
    return BackhaulGroup(
        code = code,
        title = title,
        stopCount = 0,
        pinned = false,
        quantity = 0,
        margin = 0.0,
        score = 0.0,
        depot = "",
        expiresAt = 0,
        status = BackhaulGroupStatus.PROPOSED,
    )
}
