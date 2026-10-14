package com.acme.transport.linehaul.laneplan.planwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional lane plan budget is in its life cycle. */
enum class RegionalLanePlanBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional lane plan budget. Its code is unique within a tenant. */
data class RegionalLanePlanBudget(
    var code: String,
    var label: String,
    var revision: Int,
    var durationMinutes: Int,
    var updatedAt: Int,
    var summary: String,
    var margin: Double,
    var status: RegionalLanePlanBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLanePlanBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLanePlanBudgetStatus): Boolean {
        when (status) {
            RegionalLanePlanBudgetStatus.CREATED -> return next == RegionalLanePlanBudgetStatus.VALIDATED || next == RegionalLanePlanBudgetStatus.RETIRED
            RegionalLanePlanBudgetStatus.VALIDATED -> {
                when (next) {
                    RegionalLanePlanBudgetStatus.PUBLISHED, RegionalLanePlanBudgetStatus.RETIRED -> return true
                }
                return false
            }
            RegionalLanePlanBudgetStatus.PUBLISHED -> {
                when (next) {
                    RegionalLanePlanBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLanePlanBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional lane plan budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((margin - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalLanePlanBudget {
        return RegionalLanePlanBudget(
            code = code,
            label = label,
            revision = revision,
            durationMinutes = durationMinutes,
            updatedAt = updatedAt,
            summary = summary,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional lane plan budget in status created. */
fun newRegionalLanePlanBudget(code: String, label: String): RegionalLanePlanBudget {
    return RegionalLanePlanBudget(
        code = code,
        label = label,
        revision = 0,
        durationMinutes = 0,
        updatedAt = 0,
        summary = "",
        margin = 0.0,
        status = RegionalLanePlanBudgetStatus.CREATED,
    )
}
