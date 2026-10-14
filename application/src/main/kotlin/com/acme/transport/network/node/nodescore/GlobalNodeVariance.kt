package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global node variance is in its life cycle. */
enum class GlobalNodeVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a global node variance and its current status. */
data class GlobalNodeVariance(
    var code: String,
    var title: String,
    var maximum: Double,
    var summary: String,
    var lane: String,
    var dock: String,
    var capacity: Int,
    var pallets: Int,
    var active: Boolean,
    var status: GlobalNodeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalNodeVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalNodeVarianceStatus): Boolean {
        when (status) {
            GlobalNodeVarianceStatus.DRAFT -> return next == GlobalNodeVarianceStatus.CLOSED
            GlobalNodeVarianceStatus.PENDING -> return next == GlobalNodeVarianceStatus.SUSPENDED || next == GlobalNodeVarianceStatus.CLOSED
            GlobalNodeVarianceStatus.ACTIVE -> return next == GlobalNodeVarianceStatus.SUSPENDED || next == GlobalNodeVarianceStatus.CLOSED
            GlobalNodeVarianceStatus.SUSPENDED -> return next == GlobalNodeVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalNodeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("global node variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((capacity.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): GlobalNodeVariance {
        return GlobalNodeVariance(
            code = code,
            title = title,
            maximum = maximum,
            summary = summary,
            lane = lane,
            dock = dock,
            capacity = capacity,
            pallets = pallets,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global node variance in status draft. */
fun newGlobalNodeVariance(code: String, title: String): GlobalNodeVariance {
    return GlobalNodeVariance(
        code = code,
        title = title,
        maximum = 0.0,
        summary = "",
        lane = "",
        dock = "",
        capacity = 0,
        pallets = 0,
        active = false,
        status = GlobalNodeVarianceStatus.DRAFT,
    )
}
