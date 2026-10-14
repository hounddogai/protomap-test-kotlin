package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted duty period claim is in its life cycle. */
enum class WeightedDutyPeriodClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted duty period claim. Its code is unique within a tenant. */
data class WeightedDutyPeriodClaim(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var quantity: Int,
    var region: String,
    var durationMinutes: Int,
    var pinned: Boolean,
    var status: WeightedDutyPeriodClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDutyPeriodClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDutyPeriodClaimStatus): Boolean {
        when (status) {
            WeightedDutyPeriodClaimStatus.PLANNED -> {
                when (next) {
                    WeightedDutyPeriodClaimStatus.IN_PROGRESS, WeightedDutyPeriodClaimStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedDutyPeriodClaimStatus.SCHEDULED -> {
                when (next) {
                    WeightedDutyPeriodClaimStatus.IN_PROGRESS, WeightedDutyPeriodClaimStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedDutyPeriodClaimStatus.IN_PROGRESS -> return next == WeightedDutyPeriodClaimStatus.COMPLETED || next == WeightedDutyPeriodClaimStatus.CANCELLED
            WeightedDutyPeriodClaimStatus.COMPLETED -> {
                when (next) {
                    WeightedDutyPeriodClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDutyPeriodClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted duty period claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted duty period claim"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((durationMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedDutyPeriodClaim {
        return WeightedDutyPeriodClaim(
            code = code,
            title = title,
            attributes = attributes,
            visible = visible,
            quantity = quantity,
            region = region,
            durationMinutes = durationMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted duty period claim in status planned. */
fun newWeightedDutyPeriodClaim(code: String, title: String): WeightedDutyPeriodClaim {
    return WeightedDutyPeriodClaim(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        quantity = 0,
        region = "",
        durationMinutes = 0,
        pinned = false,
        status = WeightedDutyPeriodClaimStatus.PLANNED,
    )
}
