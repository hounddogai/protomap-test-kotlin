package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted capacity draft is in its life cycle. */
enum class WeightedCapacityDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted capacity draft as the capacity team keeps it. */
data class WeightedCapacityDraft(
    var code: String,
    var title: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var visible: Boolean,
    var status: WeightedCapacityDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCapacityDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCapacityDraftStatus): Boolean {
        when (status) {
            WeightedCapacityDraftStatus.REQUESTED -> {
                when (next) {
                    WeightedCapacityDraftStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedCapacityDraftStatus.GRANTED -> {
                when (next) {
                    WeightedCapacityDraftStatus.REVOKED -> return true
                }
                return false
            }
            WeightedCapacityDraftStatus.REVOKED -> {
                when (next) {
                    WeightedCapacityDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCapacityDraftStatus) {
        if (!canMoveTo(next)) {
            error("weighted capacity draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round(stopCount.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): WeightedCapacityDraft {
        return WeightedCapacityDraft(
            code = code,
            title = title,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted capacity draft in status requested. */
fun newWeightedCapacityDraft(code: String, title: String): WeightedCapacityDraft {
    return WeightedCapacityDraft(
        code = code,
        title = title,
        stopCount = 0,
        etaMinutes = 0,
        visible = false,
        status = WeightedCapacityDraftStatus.REQUESTED,
    )
}
