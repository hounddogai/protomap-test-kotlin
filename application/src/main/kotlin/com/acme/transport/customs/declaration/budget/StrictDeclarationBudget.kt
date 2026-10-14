package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict declaration budget records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a strict declaration budget is in its life cycle. */
enum class StrictDeclarationBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict declaration budget as the declaration team keeps it. */
data class StrictDeclarationBudget(
    var code: String,
    var label: String,
    var externalRef: String,
    var endsAt: Int,
    var dock: String,
    var odometer: Int,
    var ceiling: Double,
    var status: StrictDeclarationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDeclarationBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDeclarationBudgetStatus): Boolean {
        when (status) {
            StrictDeclarationBudgetStatus.CREATED -> return next == StrictDeclarationBudgetStatus.VALIDATED || next == StrictDeclarationBudgetStatus.PUBLISHED
            StrictDeclarationBudgetStatus.VALIDATED -> return next == StrictDeclarationBudgetStatus.RETIRED
            StrictDeclarationBudgetStatus.PUBLISHED -> {
                when (next) {
                    StrictDeclarationBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDeclarationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("strict declaration budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round(ceiling * endsAt.toDouble() * 100) / 100

    fun snapshot(): StrictDeclarationBudget {
        return StrictDeclarationBudget(
            code = code,
            label = label,
            externalRef = externalRef,
            endsAt = endsAt,
            dock = dock,
            odometer = odometer,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict declaration budget in status created. */
fun newStrictDeclarationBudget(code: String, label: String): StrictDeclarationBudget {
    return StrictDeclarationBudget(
        code = code,
        label = label,
        externalRef = "",
        endsAt = 0,
        dock = "",
        odometer = 0,
        ceiling = 0.0,
        status = StrictDeclarationBudgetStatus.CREATED,
    )
}
