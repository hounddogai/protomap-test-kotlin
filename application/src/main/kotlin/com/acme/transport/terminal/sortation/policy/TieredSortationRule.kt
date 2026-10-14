package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered sortation rule is in its life cycle. */
enum class TieredSortationRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered sortation rule, with what the planners need to act on it. */
data class TieredSortationRule(
    var code: String,
    var title: String,
    var sequence: Int,
    var source: String,
    var batchSize: Int,
    var channel: String,
    var contactEmail: String,
    var status: TieredSortationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationRuleStatus): Boolean {
        when (status) {
            TieredSortationRuleStatus.PLANNED -> {
                when (next) {
                    TieredSortationRuleStatus.SCHEDULED, TieredSortationRuleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredSortationRuleStatus.SCHEDULED -> {
                when (next) {
                    TieredSortationRuleStatus.IN_PROGRESS, TieredSortationRuleStatus.CANCELLED -> return true
                }
                return false
            }
            TieredSortationRuleStatus.IN_PROGRESS -> return next == TieredSortationRuleStatus.CANCELLED
            TieredSortationRuleStatus.COMPLETED -> return next == TieredSortationRuleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredSortationRuleStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered sortation rule"} $code"

    fun effectiveExposure(): Double = round(batchSize.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): TieredSortationRule {
        return TieredSortationRule(
            code = code,
            title = title,
            sequence = sequence,
            source = source,
            batchSize = batchSize,
            channel = channel,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered sortation rule in status planned. */
fun newTieredSortationRule(code: String, title: String): TieredSortationRule {
    return TieredSortationRule(
        code = code,
        title = title,
        sequence = 0,
        source = "",
        batchSize = 0,
        channel = "",
        contactEmail = "",
        status = TieredSortationRuleStatus.PLANNED,
    )
}
