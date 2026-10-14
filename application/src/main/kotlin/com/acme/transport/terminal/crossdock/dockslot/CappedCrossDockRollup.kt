package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cross dock rollup is in its life cycle. */
enum class CappedCrossDockRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capped cross dock rollup, with what the planners need to act on it. */
data class CappedCrossDockRollup(
    var code: String,
    var name: String,
    var archived: Boolean,
    var weight: Double,
    var reviewer: String,
    var ceiling: Double,
    var budget: Double,
    var summary: String,
    var loadKg: Double,
    var approved: Boolean,
    var status: CappedCrossDockRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCrossDockRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCrossDockRollupStatus): Boolean {
        when (status) {
            CappedCrossDockRollupStatus.REQUESTED -> {
                when (next) {
                    CappedCrossDockRollupStatus.GRANTED, CappedCrossDockRollupStatus.REVOKED -> return true
                }
                return false
            }
            CappedCrossDockRollupStatus.GRANTED -> return next == CappedCrossDockRollupStatus.REVOKED || next == CappedCrossDockRollupStatus.EXPIRED
            CappedCrossDockRollupStatus.REVOKED -> return next == CappedCrossDockRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedCrossDockRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped cross dock rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((loadKg + weight) * 100) / 100

    fun snapshot(): CappedCrossDockRollup {
        return CappedCrossDockRollup(
            code = code,
            name = name,
            archived = archived,
            weight = weight,
            reviewer = reviewer,
            ceiling = ceiling,
            budget = budget,
            summary = summary,
            loadKg = loadKg,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped cross dock rollup in status requested. */
fun newCappedCrossDockRollup(code: String, name: String): CappedCrossDockRollup {
    return CappedCrossDockRollup(
        code = code,
        name = name,
        archived = false,
        weight = 0.0,
        reviewer = "",
        ceiling = 0.0,
        budget = 0.0,
        summary = "",
        loadKg = 0.0,
        approved = false,
        status = CappedCrossDockRollupStatus.REQUESTED,
    )
}
