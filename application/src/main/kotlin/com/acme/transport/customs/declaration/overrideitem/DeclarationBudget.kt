package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration budget is in its life cycle. */
enum class DeclarationBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a declaration budget and its current status. */
data class DeclarationBudget(
    var code: String,
    var title: String,
    var dock: String,
    var approved: Boolean,
    var carrier: String,
    var rate: Double,
    var channel: String,
    var category: String,
    var maximum: Double,
    var notes: String,
    var timezone: String,
    var status: DeclarationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationBudgetStatus): Boolean {
        when (status) {
            DeclarationBudgetStatus.IDLE -> {
                when (next) {
                    DeclarationBudgetStatus.READY, DeclarationBudgetStatus.DRAINING -> return true
                }
                return false
            }
            DeclarationBudgetStatus.WARMING -> return next == DeclarationBudgetStatus.STOPPED
            DeclarationBudgetStatus.READY -> return next == DeclarationBudgetStatus.DRAINING || next == DeclarationBudgetStatus.STOPPED
            DeclarationBudgetStatus.DRAINING -> {
                when (next) {
                    DeclarationBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("declaration budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((maximum - rate) * 100) / 100

    fun snapshot(): DeclarationBudget {
        return DeclarationBudget(
            code = code,
            title = title,
            dock = dock,
            approved = approved,
            carrier = carrier,
            rate = rate,
            channel = channel,
            category = category,
            maximum = maximum,
            notes = notes,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a declaration budget in status idle. */
fun newDeclarationBudget(code: String, title: String): DeclarationBudget {
    return DeclarationBudget(
        code = code,
        title = title,
        dock = "",
        approved = false,
        carrier = "",
        rate = 0.0,
        channel = "",
        category = "",
        maximum = 0.0,
        notes = "",
        timezone = "",
        status = DeclarationBudgetStatus.IDLE,
    )
}
