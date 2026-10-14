package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect rule is in its life cycle. */
enum class DefectRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One defect rule, with what the planners need to act on it. */
data class DefectRule(
    var code: String,
    var label: String,
    var depot: String,
    var minimum: Double,
    var etaMinutes: Int,
    var channel: String,
    var tags: MutableList<String>,
    var score: Double,
    var status: DefectRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectRuleStatus): Boolean {
        when (status) {
            DefectRuleStatus.PLANNED -> return next == DefectRuleStatus.SCHEDULED || next == DefectRuleStatus.COMPLETED
            DefectRuleStatus.SCHEDULED -> {
                when (next) {
                    DefectRuleStatus.IN_PROGRESS, DefectRuleStatus.COMPLETED -> return true
                }
                return false
            }
            DefectRuleStatus.IN_PROGRESS -> return next == DefectRuleStatus.COMPLETED || next == DefectRuleStatus.CANCELLED
            DefectRuleStatus.COMPLETED -> return next == DefectRuleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefectRuleStatus) {
        if (!canMoveTo(next)) {
            error("defect rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((score - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefectRule {
        return DefectRule(
            code = code,
            label = label,
            depot = depot,
            minimum = minimum,
            etaMinutes = etaMinutes,
            channel = channel,
            tags = tags,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a defect rule in status planned. */
fun newDefectRule(code: String, label: String): DefectRule {
    return DefectRule(
        code = code,
        label = label,
        depot = "",
        minimum = 0.0,
        etaMinutes = 0,
        channel = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        status = DefectRuleStatus.PLANNED,
    )
}
