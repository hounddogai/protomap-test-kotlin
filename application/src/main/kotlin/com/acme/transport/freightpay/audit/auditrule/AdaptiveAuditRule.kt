package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive audit rule records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a adaptive audit rule is in its life cycle. */
enum class AdaptiveAuditRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive audit rule, with what the planners need to act on it. */
data class AdaptiveAuditRule(
    var code: String,
    var name: String,
    var weight: Double,
    var batchSize: Int,
    var channel: String,
    var minimum: Double,
    var rate: Double,
    var windowDays: Int,
    var loadKg: Double,
    var stopCount: Int,
    var ratio: Double,
    var status: AdaptiveAuditRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditRuleStatus): Boolean {
        when (status) {
            AdaptiveAuditRuleStatus.CREATED -> {
                when (next) {
                    AdaptiveAuditRuleStatus.PUBLISHED, AdaptiveAuditRuleStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveAuditRuleStatus.VALIDATED -> return next == AdaptiveAuditRuleStatus.PUBLISHED || next == AdaptiveAuditRuleStatus.RETIRED
            AdaptiveAuditRuleStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveAuditRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditRuleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive audit rule"} $code"

    fun weightedVolume(): Double = round(ratio * minimum * 100) / 100

    fun snapshot(): AdaptiveAuditRule {
        return AdaptiveAuditRule(
            code = code,
            name = name,
            weight = weight,
            batchSize = batchSize,
            channel = channel,
            minimum = minimum,
            rate = rate,
            windowDays = windowDays,
            loadKg = loadKg,
            stopCount = stopCount,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive audit rule in status created. */
fun newAdaptiveAuditRule(code: String, name: String): AdaptiveAuditRule {
    return AdaptiveAuditRule(
        code = code,
        name = name,
        weight = 0.0,
        batchSize = 0,
        channel = "",
        minimum = 0.0,
        rate = 0.0,
        windowDays = 0,
        loadKg = 0.0,
        stopCount = 0,
        ratio = 0.0,
        status = AdaptiveAuditRuleStatus.CREATED,
    )
}
