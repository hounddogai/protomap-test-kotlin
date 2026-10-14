package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door segment is in its life cycle. */
enum class DockDoorSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dock door segment. Its code is unique within a tenant. */
data class DockDoorSegment(
    var code: String,
    var label: String,
    var locale: String,
    var weight: Double,
    var expiresAt: Int,
    var threshold: Double,
    var cost: Double,
    var status: DockDoorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorSegmentStatus): Boolean {
        when (status) {
            DockDoorSegmentStatus.NEW -> {
                when (next) {
                    DockDoorSegmentStatus.RUNNING, DockDoorSegmentStatus.DONE -> return true
                }
                return false
            }
            DockDoorSegmentStatus.QUEUED -> return next == DockDoorSegmentStatus.RUNNING || next == DockDoorSegmentStatus.FAILED
            DockDoorSegmentStatus.RUNNING -> {
                when (next) {
                    DockDoorSegmentStatus.FAILED -> return true
                }
                return false
            }
            DockDoorSegmentStatus.DONE -> {
                when (next) {
                    DockDoorSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("dock door segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((weight - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): DockDoorSegment {
        return DockDoorSegment(
            code = code,
            label = label,
            locale = locale,
            weight = weight,
            expiresAt = expiresAt,
            threshold = threshold,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dock door segment in status new. */
fun newDockDoorSegment(code: String, label: String): DockDoorSegment {
    return DockDoorSegment(
        code = code,
        label = label,
        locale = "",
        weight = 0.0,
        expiresAt = 0,
        threshold = 0.0,
        cost = 0.0,
        status = DockDoorSegmentStatus.NEW,
    )
}
