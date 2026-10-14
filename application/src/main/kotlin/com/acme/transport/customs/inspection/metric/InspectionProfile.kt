package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection profile is in its life cycle. */
enum class InspectionProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A inspection profile as the inspection team keeps it. */
data class InspectionProfile(
    var code: String,
    var label: String,
    var cost: Double,
    var expiresAt: Int,
    var rate: Double,
    var tags: MutableList<String>,
    var unit: String,
    var status: InspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionProfileStatus): Boolean {
        when (status) {
            InspectionProfileStatus.IDLE -> return next == InspectionProfileStatus.READY || next == InspectionProfileStatus.STOPPED
            InspectionProfileStatus.WARMING -> {
                when (next) {
                    InspectionProfileStatus.STOPPED -> return true
                }
                return false
            }
            InspectionProfileStatus.READY -> return next == InspectionProfileStatus.STOPPED
            InspectionProfileStatus.DRAINING -> {
                when (next) {
                    InspectionProfileStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("inspection profile $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((rate - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionProfile {
        return InspectionProfile(
            code = code,
            label = label,
            cost = cost,
            expiresAt = expiresAt,
            rate = rate,
            tags = tags,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection profile in status idle. */
fun newInspectionProfile(code: String, label: String): InspectionProfile {
    return InspectionProfile(
        code = code,
        label = label,
        cost = 0.0,
        expiresAt = 0,
        rate = 0.0,
        tags = mutableListOf<String>(),
        unit = "",
        status = InspectionProfileStatus.IDLE,
    )
}
