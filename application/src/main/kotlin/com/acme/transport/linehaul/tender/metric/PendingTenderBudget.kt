package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tender budget is in its life cycle. */
enum class PendingTenderBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending tender budget as the tender team keeps it. */
data class PendingTenderBudget(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var shiftCode: String,
    var active: Boolean,
    var durationMinutes: Int,
    var floorValue: Double,
    var status: PendingTenderBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderBudgetStatus): Boolean {
        when (status) {
            PendingTenderBudgetStatus.REQUESTED -> {
                when (next) {
                    PendingTenderBudgetStatus.GRANTED, PendingTenderBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            PendingTenderBudgetStatus.GRANTED -> {
                when (next) {
                    PendingTenderBudgetStatus.REVOKED, PendingTenderBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            PendingTenderBudgetStatus.REVOKED -> return next == PendingTenderBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingTenderBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending tender budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round(updatedAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): PendingTenderBudget {
        return PendingTenderBudget(
            code = code,
            title = title,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            active = active,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending tender budget in status requested. */
fun newPendingTenderBudget(code: String, title: String): PendingTenderBudget {
    return PendingTenderBudget(
        code = code,
        title = title,
        updatedAt = 0,
        shiftCode = "",
        active = false,
        durationMinutes = 0,
        floorValue = 0.0,
        status = PendingTenderBudgetStatus.REQUESTED,
    )
}
