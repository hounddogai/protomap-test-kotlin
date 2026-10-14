package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tender rule is in its life cycle. */
enum class ManualTenderRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual tender rule. Its code is unique within a tenant. */
data class ManualTenderRule(
    var code: String,
    var label: String,
    var active: Boolean,
    var weight: Double,
    var attempts: Int,
    var status: ManualTenderRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderRuleStatus): Boolean {
        when (status) {
            ManualTenderRuleStatus.IDLE -> {
                when (next) {
                    ManualTenderRuleStatus.STOPPED -> return true
                }
                return false
            }
            ManualTenderRuleStatus.WARMING -> return next == ManualTenderRuleStatus.DRAINING || next == ManualTenderRuleStatus.STOPPED
            ManualTenderRuleStatus.READY -> {
                when (next) {
                    ManualTenderRuleStatus.DRAINING -> return true
                }
                return false
            }
            ManualTenderRuleStatus.DRAINING -> return next == ManualTenderRuleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualTenderRuleStatus) {
        if (!canMoveTo(next)) {
            error("manual tender rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round((attempts.toDouble() + weight) * 100) / 100

    fun snapshot(): ManualTenderRule {
        return ManualTenderRule(
            code = code,
            label = label,
            active = active,
            weight = weight,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual tender rule in status idle. */
fun newManualTenderRule(code: String, label: String): ManualTenderRule {
    return ManualTenderRule(
        code = code,
        label = label,
        active = false,
        weight = 0.0,
        attempts = 0,
        status = ManualTenderRuleStatus.IDLE,
    )
}
