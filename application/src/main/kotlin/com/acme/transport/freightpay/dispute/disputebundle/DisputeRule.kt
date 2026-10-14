package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute rule is in its life cycle. */
enum class DisputeRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One dispute rule, with what the planners need to act on it. */
data class DisputeRule(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var source: String,
    var threshold: Double,
    var checksum: String,
    var driverId: String,
    var ratio: Double,
    var region: String,
    var status: DisputeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeRuleStatus): Boolean {
        when (status) {
            DisputeRuleStatus.REQUESTED -> {
                when (next) {
                    DisputeRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DisputeRuleStatus.GRANTED -> {
                when (next) {
                    DisputeRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DisputeRuleStatus.REVOKED -> return next == DisputeRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DisputeRuleStatus) {
        if (!canMoveTo(next)) {
            error("dispute rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((ratio - threshold) * 100) / 100

    fun snapshot(): DisputeRule {
        return DisputeRule(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            source = source,
            threshold = threshold,
            checksum = checksum,
            driverId = driverId,
            ratio = ratio,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dispute rule in status requested. */
fun newDisputeRule(code: String, title: String): DisputeRule {
    return DisputeRule(
        code = code,
        title = title,
        durationMinutes = 0,
        source = "",
        threshold = 0.0,
        checksum = "",
        driverId = "",
        ratio = 0.0,
        region = "",
        status = DisputeRuleStatus.REQUESTED,
    )
}
