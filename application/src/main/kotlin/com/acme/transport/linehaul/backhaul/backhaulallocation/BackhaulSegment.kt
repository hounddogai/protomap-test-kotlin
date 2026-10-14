package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul segment is in its life cycle. */
enum class BackhaulSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A backhaul segment. Its code is unique within a tenant. */
data class BackhaulSegment(
    var code: String,
    var label: String,
    var windowDays: Int,
    var reviewer: String,
    var loadKg: Double,
    var unit: String,
    var status: BackhaulSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulSegmentStatus): Boolean {
        when (status) {
            BackhaulSegmentStatus.REQUESTED -> {
                when (next) {
                    BackhaulSegmentStatus.GRANTED, BackhaulSegmentStatus.REVOKED -> return true
                }
                return false
            }
            BackhaulSegmentStatus.GRANTED -> {
                when (next) {
                    BackhaulSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            BackhaulSegmentStatus.REVOKED -> {
                when (next) {
                    BackhaulSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulSegmentStatus) {
        if (!canMoveTo(next)) {
            error("backhaul segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"backhaul segment"} $code"

    fun projectedVolume(): Double = round((windowDays.toDouble() + loadKg) * 100) / 100

    fun snapshot(): BackhaulSegment {
        return BackhaulSegment(
            code = code,
            label = label,
            windowDays = windowDays,
            reviewer = reviewer,
            loadKg = loadKg,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a backhaul segment in status requested. */
fun newBackhaulSegment(code: String, label: String): BackhaulSegment {
    return BackhaulSegment(
        code = code,
        label = label,
        windowDays = 0,
        reviewer = "",
        loadKg = 0.0,
        unit = "",
        status = BackhaulSegmentStatus.REQUESTED,
    )
}
