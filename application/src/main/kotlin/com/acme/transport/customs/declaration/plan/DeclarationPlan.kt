package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration plan records of customs's declaration work, and the rules, stores, and calculations around
// them.

/** Where a declaration plan is in its life cycle. */
enum class DeclarationPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a declaration plan and its current status. */
data class DeclarationPlan(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var score: Double,
    var limit: Int,
    var visible: Boolean,
    var source: String,
    var currency: String,
    var retries: Int,
    var status: DeclarationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationPlanStatus): Boolean {
        when (status) {
            DeclarationPlanStatus.PLANNED -> return next == DeclarationPlanStatus.COMPLETED
            DeclarationPlanStatus.SCHEDULED -> return next == DeclarationPlanStatus.COMPLETED || next == DeclarationPlanStatus.CANCELLED
            DeclarationPlanStatus.IN_PROGRESS -> return next == DeclarationPlanStatus.COMPLETED
            DeclarationPlanStatus.COMPLETED -> return next == DeclarationPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DeclarationPlanStatus) {
        if (!canMoveTo(next)) {
            error("declaration plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((score - retries.toDouble()) * 100) / 100

    fun snapshot(): DeclarationPlan {
        return DeclarationPlan(
            code = code,
            name = name,
            pinned = pinned,
            score = score,
            limit = limit,
            visible = visible,
            source = source,
            currency = currency,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a declaration plan in status planned. */
fun newDeclarationPlan(code: String, name: String): DeclarationPlan {
    return DeclarationPlan(
        code = code,
        name = name,
        pinned = false,
        score = 0.0,
        limit = 0,
        visible = false,
        source = "",
        currency = "",
        retries = 0,
        status = DeclarationPlanStatus.PLANNED,
    )
}
