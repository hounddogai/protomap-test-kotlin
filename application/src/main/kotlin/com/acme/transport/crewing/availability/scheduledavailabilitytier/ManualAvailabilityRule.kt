package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual availability rule is in its life cycle. */
enum class ManualAvailabilityRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual availability rule and its current status. */
data class ManualAvailabilityRule(
    var code: String,
    var title: String,
    var reviewer: String,
    var description: String,
    var createdAt: Int,
    var stopCount: Int,
    var margin: Double,
    var dock: String,
    var status: ManualAvailabilityRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAvailabilityRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAvailabilityRuleStatus): Boolean {
        when (status) {
            ManualAvailabilityRuleStatus.CREATED -> {
                when (next) {
                    ManualAvailabilityRuleStatus.VALIDATED, ManualAvailabilityRuleStatus.RETIRED -> return true
                }
                return false
            }
            ManualAvailabilityRuleStatus.VALIDATED -> {
                when (next) {
                    ManualAvailabilityRuleStatus.RETIRED -> return true
                }
                return false
            }
            ManualAvailabilityRuleStatus.PUBLISHED -> {
                when (next) {
                    ManualAvailabilityRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAvailabilityRuleStatus) {
        if (!canMoveTo(next)) {
            error("manual availability rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual availability rule"} $code"

    fun effectiveTotal(): Double = round((createdAt.toDouble() - margin) * 100) / 100

    fun snapshot(): ManualAvailabilityRule {
        return ManualAvailabilityRule(
            code = code,
            title = title,
            reviewer = reviewer,
            description = description,
            createdAt = createdAt,
            stopCount = stopCount,
            margin = margin,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual availability rule in status created. */
fun newManualAvailabilityRule(code: String, title: String): ManualAvailabilityRule {
    return ManualAvailabilityRule(
        code = code,
        title = title,
        reviewer = "",
        description = "",
        createdAt = 0,
        stopCount = 0,
        margin = 0.0,
        dock = "",
        status = ManualAvailabilityRuleStatus.CREATED,
    )
}
