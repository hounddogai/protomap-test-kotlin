package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation rule is in its life cycle. */
enum class SortationRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A sortation rule as the sortation team keeps it. */
data class SortationRule(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var source: String,
    var status: SortationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationRuleStatus): Boolean {
        when (status) {
            SortationRuleStatus.PLANNED -> {
                when (next) {
                    SortationRuleStatus.IN_PROGRESS, SortationRuleStatus.CANCELLED -> return true
                }
                return false
            }
            SortationRuleStatus.SCHEDULED -> {
                when (next) {
                    SortationRuleStatus.COMPLETED, SortationRuleStatus.CANCELLED -> return true
                }
                return false
            }
            SortationRuleStatus.IN_PROGRESS -> {
                when (next) {
                    SortationRuleStatus.COMPLETED -> return true
                }
                return false
            }
            SortationRuleStatus.COMPLETED -> return next == SortationRuleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SortationRuleStatus) {
        if (!canMoveTo(next)) {
            error("sortation rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round(windowDays.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): SortationRule {
        return SortationRule(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a sortation rule in status planned. */
fun newSortationRule(code: String, title: String): SortationRule {
    return SortationRule(
        code = code,
        title = title,
        etaMinutes = 0,
        windowDays = 0,
        source = "",
        status = SortationRuleStatus.PLANNED,
    )
}
