package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff segment records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff segment is in its life cycle. */
enum class CutoffSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff segment. Its code is unique within a tenant. */
data class CutoffSegment(
    var code: String,
    var name: String,
    var summary: String,
    var category: String,
    var endsAt: Int,
    var expiresAt: Int,
    var visible: Boolean,
    var vehicleId: String,
    var windowDays: Int,
    var status: CutoffSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffSegmentStatus): Boolean {
        when (status) {
            CutoffSegmentStatus.NEW -> return next == CutoffSegmentStatus.QUEUED || next == CutoffSegmentStatus.FAILED
            CutoffSegmentStatus.QUEUED -> return next == CutoffSegmentStatus.DONE
            CutoffSegmentStatus.RUNNING -> return next == CutoffSegmentStatus.DONE || next == CutoffSegmentStatus.FAILED
            CutoffSegmentStatus.DONE -> {
                when (next) {
                    CutoffSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffSegmentStatus) {
        if (!canMoveTo(next)) {
            error("cutoff segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round((windowDays.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CutoffSegment {
        return CutoffSegment(
            code = code,
            name = name,
            summary = summary,
            category = category,
            endsAt = endsAt,
            expiresAt = expiresAt,
            visible = visible,
            vehicleId = vehicleId,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a cutoff segment in status new. */
fun newCutoffSegment(code: String, name: String): CutoffSegment {
    return CutoffSegment(
        code = code,
        name = name,
        summary = "",
        category = "",
        endsAt = 0,
        expiresAt = 0,
        visible = false,
        vehicleId = "",
        windowDays = 0,
        status = CutoffSegmentStatus.NEW,
    )
}
