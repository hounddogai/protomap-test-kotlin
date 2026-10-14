package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan adjustment is in its life cycle. */
enum class LanePlanAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lane plan adjustment as the lane plan team keeps it. */
data class LanePlanAdjustment(
    var code: String,
    var name: String,
    var retries: Int,
    var active: Boolean,
    var reviewer: String,
    var createdAt: Int,
    var etaMinutes: Int,
    var status: LanePlanAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanAdjustmentStatus): Boolean {
        when (status) {
            LanePlanAdjustmentStatus.DRAFT -> return next == LanePlanAdjustmentStatus.PENDING || next == LanePlanAdjustmentStatus.CLOSED
            LanePlanAdjustmentStatus.PENDING -> return next == LanePlanAdjustmentStatus.ACTIVE
            LanePlanAdjustmentStatus.ACTIVE -> return next == LanePlanAdjustmentStatus.SUSPENDED || next == LanePlanAdjustmentStatus.CLOSED
            LanePlanAdjustmentStatus.SUSPENDED -> {
                when (next) {
                    LanePlanAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("lane plan adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lane plan adjustment"} $code"

    fun effectiveTotal(): Double = round((etaMinutes.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): LanePlanAdjustment {
        return LanePlanAdjustment(
            code = code,
            name = name,
            retries = retries,
            active = active,
            reviewer = reviewer,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan adjustment in status draft. */
fun newLanePlanAdjustment(code: String, name: String): LanePlanAdjustment {
    return LanePlanAdjustment(
        code = code,
        name = name,
        retries = 0,
        active = false,
        reviewer = "",
        createdAt = 0,
        etaMinutes = 0,
        status = LanePlanAdjustmentStatus.DRAFT,
    )
}
