package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback defect rule is in its life cycle. */
enum class FallbackDefectRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback defect rule as the defect team keeps it. */
data class FallbackDefectRule(
    var code: String,
    var title: String,
    var createdAt: Int,
    var reviewer: String,
    var capacity: Int,
    var minimum: Double,
    var status: FallbackDefectRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDefectRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDefectRuleStatus): Boolean {
        when (status) {
            FallbackDefectRuleStatus.IDLE -> {
                when (next) {
                    FallbackDefectRuleStatus.STOPPED -> return true
                }
                return false
            }
            FallbackDefectRuleStatus.WARMING -> return next == FallbackDefectRuleStatus.READY || next == FallbackDefectRuleStatus.STOPPED
            FallbackDefectRuleStatus.READY -> {
                when (next) {
                    FallbackDefectRuleStatus.DRAINING, FallbackDefectRuleStatus.STOPPED -> return true
                }
                return false
            }
            FallbackDefectRuleStatus.DRAINING -> {
                when (next) {
                    FallbackDefectRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDefectRuleStatus) {
        if (!canMoveTo(next)) {
            error("fallback defect rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback defect rule"} $code"

    fun netVolume(): Double = round((createdAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): FallbackDefectRule {
        return FallbackDefectRule(
            code = code,
            title = title,
            createdAt = createdAt,
            reviewer = reviewer,
            capacity = capacity,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback defect rule in status idle. */
fun newFallbackDefectRule(code: String, title: String): FallbackDefectRule {
    return FallbackDefectRule(
        code = code,
        title = title,
        createdAt = 0,
        reviewer = "",
        capacity = 0,
        minimum = 0.0,
        status = FallbackDefectRuleStatus.IDLE,
    )
}
