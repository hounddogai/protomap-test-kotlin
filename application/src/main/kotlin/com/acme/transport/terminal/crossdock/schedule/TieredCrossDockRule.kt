package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cross dock rule is in its life cycle. */
enum class TieredCrossDockRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered cross dock rule as the cross dock team keeps it. */
data class TieredCrossDockRule(
    var code: String,
    var label: String,
    var archived: Boolean,
    var expiresAt: Int,
    var batchSize: Int,
    var threshold: Double,
    var timezone: String,
    var visible: Boolean,
    var approved: Boolean,
    var status: TieredCrossDockRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockRuleStatus): Boolean {
        when (status) {
            TieredCrossDockRuleStatus.PROPOSED -> return next == TieredCrossDockRuleStatus.APPROVED
            TieredCrossDockRuleStatus.APPROVED -> return next == TieredCrossDockRuleStatus.REJECTED || next == TieredCrossDockRuleStatus.WITHDRAWN
            TieredCrossDockRuleStatus.REJECTED -> {
                when (next) {
                    TieredCrossDockRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCrossDockRuleStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered cross dock rule"} $code"

    fun weightedValue(): Double = round(expiresAt.toDouble() * threshold * 100) / 100

    fun snapshot(): TieredCrossDockRule {
        return TieredCrossDockRule(
            code = code,
            label = label,
            archived = archived,
            expiresAt = expiresAt,
            batchSize = batchSize,
            threshold = threshold,
            timezone = timezone,
            visible = visible,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered cross dock rule in status proposed. */
fun newTieredCrossDockRule(code: String, label: String): TieredCrossDockRule {
    return TieredCrossDockRule(
        code = code,
        label = label,
        archived = false,
        expiresAt = 0,
        batchSize = 0,
        threshold = 0.0,
        timezone = "",
        visible = false,
        approved = false,
        status = TieredCrossDockRuleStatus.PROPOSED,
    )
}
