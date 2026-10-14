package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional drop target is in its life cycle. */
enum class RegionalDropTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional drop target and its current status. */
data class RegionalDropTarget(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var updatedAt: Int,
    var minimum: Double,
    var trailerId: String,
    var visible: Boolean,
    var source: String,
    var summary: String,
    var status: RegionalDropTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropTargetStatus): Boolean {
        when (status) {
            RegionalDropTargetStatus.NEW -> return next == RegionalDropTargetStatus.QUEUED || next == RegionalDropTargetStatus.FAILED
            RegionalDropTargetStatus.QUEUED -> return next == RegionalDropTargetStatus.RUNNING || next == RegionalDropTargetStatus.FAILED
            RegionalDropTargetStatus.RUNNING -> {
                when (next) {
                    RegionalDropTargetStatus.DONE, RegionalDropTargetStatus.FAILED -> return true
                }
                return false
            }
            RegionalDropTargetStatus.DONE -> {
                when (next) {
                    RegionalDropTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDropTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional drop target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional drop target"} $code"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalDropTarget {
        return RegionalDropTarget(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            updatedAt = updatedAt,
            minimum = minimum,
            trailerId = trailerId,
            visible = visible,
            source = source,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional drop target in status new. */
fun newRegionalDropTarget(code: String, title: String): RegionalDropTarget {
    return RegionalDropTarget(
        code = code,
        title = title,
        etaMinutes = 0,
        updatedAt = 0,
        minimum = 0.0,
        trailerId = "",
        visible = false,
        source = "",
        summary = "",
        status = RegionalDropTargetStatus.NEW,
    )
}
