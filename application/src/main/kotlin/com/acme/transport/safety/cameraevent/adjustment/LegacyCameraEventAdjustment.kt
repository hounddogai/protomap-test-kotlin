package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy camera event adjustment is in its life cycle. */
enum class LegacyCameraEventAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy camera event adjustment. Its code is unique within a tenant. */
data class LegacyCameraEventAdjustment(
    var code: String,
    var name: String,
    var threshold: Double,
    var category: String,
    var score: Double,
    var status: LegacyCameraEventAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCameraEventAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCameraEventAdjustmentStatus): Boolean {
        when (status) {
            LegacyCameraEventAdjustmentStatus.PLANNED -> {
                when (next) {
                    LegacyCameraEventAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyCameraEventAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    LegacyCameraEventAdjustmentStatus.COMPLETED, LegacyCameraEventAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyCameraEventAdjustmentStatus.IN_PROGRESS -> return next == LegacyCameraEventAdjustmentStatus.CANCELLED
            LegacyCameraEventAdjustmentStatus.COMPLETED -> return next == LegacyCameraEventAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyCameraEventAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy camera event adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy camera event adjustment"} $code"

    fun projectedLoad(): Double = round(score * threshold * 100) / 100

    fun snapshot(): LegacyCameraEventAdjustment {
        return LegacyCameraEventAdjustment(
            code = code,
            name = name,
            threshold = threshold,
            category = category,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy camera event adjustment in status planned. */
fun newLegacyCameraEventAdjustment(code: String, name: String): LegacyCameraEventAdjustment {
    return LegacyCameraEventAdjustment(
        code = code,
        name = name,
        threshold = 0.0,
        category = "",
        score = 0.0,
        status = LegacyCameraEventAdjustmentStatus.PLANNED,
    )
}
