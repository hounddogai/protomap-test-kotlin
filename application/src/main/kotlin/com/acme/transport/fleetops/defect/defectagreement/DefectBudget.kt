package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect budget is in its life cycle. */
enum class DefectBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A defect budget as the defect team keeps it. */
data class DefectBudget(
    var code: String,
    var label: String,
    var archived: Boolean,
    var locale: String,
    var floorValue: Double,
    var amount: Double,
    var summary: String,
    var status: DefectBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectBudgetStatus): Boolean {
        when (status) {
            DefectBudgetStatus.REQUESTED -> return next == DefectBudgetStatus.EXPIRED
            DefectBudgetStatus.GRANTED -> return next == DefectBudgetStatus.REVOKED || next == DefectBudgetStatus.EXPIRED
            DefectBudgetStatus.REVOKED -> {
                when (next) {
                    DefectBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectBudgetStatus) {
        if (!canMoveTo(next)) {
            error("defect budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"defect budget"} $code"

    fun netValue(): Double = round((floorValue + amount) * 100) / 100

    fun snapshot(): DefectBudget {
        return DefectBudget(
            code = code,
            label = label,
            archived = archived,
            locale = locale,
            floorValue = floorValue,
            amount = amount,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a defect budget in status requested. */
fun newDefectBudget(code: String, label: String): DefectBudget {
    return DefectBudget(
        code = code,
        label = label,
        archived = false,
        locale = "",
        floorValue = 0.0,
        amount = 0.0,
        summary = "",
        status = DefectBudgetStatus.REQUESTED,
    )
}
