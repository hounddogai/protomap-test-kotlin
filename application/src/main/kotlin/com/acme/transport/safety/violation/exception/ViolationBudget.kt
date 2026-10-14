package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation budget is in its life cycle. */
enum class ViolationBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A violation budget as the violation team keeps it. */
data class ViolationBudget(
    var code: String,
    var name: String,
    var minimum: Double,
    var quantity: Int,
    var limit: Int,
    var summary: String,
    var shiftCode: String,
    var checksum: String,
    var status: ViolationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationBudgetStatus): Boolean {
        when (status) {
            ViolationBudgetStatus.PROPOSED -> {
                when (next) {
                    ViolationBudgetStatus.APPROVED, ViolationBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            ViolationBudgetStatus.APPROVED -> {
                when (next) {
                    ViolationBudgetStatus.REJECTED, ViolationBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            ViolationBudgetStatus.REJECTED -> return next == ViolationBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ViolationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("violation budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round(limit.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): ViolationBudget {
        return ViolationBudget(
            code = code,
            name = name,
            minimum = minimum,
            quantity = quantity,
            limit = limit,
            summary = summary,
            shiftCode = shiftCode,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation budget in status proposed. */
fun newViolationBudget(code: String, name: String): ViolationBudget {
    return ViolationBudget(
        code = code,
        name = name,
        minimum = 0.0,
        quantity = 0,
        limit = 0,
        summary = "",
        shiftCode = "",
        checksum = "",
        status = ViolationBudgetStatus.PROPOSED,
    )
}
