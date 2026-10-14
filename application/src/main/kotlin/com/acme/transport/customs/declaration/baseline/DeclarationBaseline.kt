package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration baseline records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration baseline is in its life cycle. */
enum class DeclarationBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a declaration baseline and its current status. */
data class DeclarationBaseline(
    var code: String,
    var name: String,
    var amount: Double,
    var rate: Double,
    var shiftCode: String,
    var status: DeclarationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationBaselineStatus): Boolean {
        when (status) {
            DeclarationBaselineStatus.DRAFT -> return next == DeclarationBaselineStatus.ACTIVE || next == DeclarationBaselineStatus.SUSPENDED
            DeclarationBaselineStatus.PENDING -> {
                when (next) {
                    DeclarationBaselineStatus.ACTIVE, DeclarationBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            DeclarationBaselineStatus.ACTIVE -> {
                when (next) {
                    DeclarationBaselineStatus.CLOSED -> return true
                }
                return false
            }
            DeclarationBaselineStatus.SUSPENDED -> {
                when (next) {
                    DeclarationBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("declaration baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((amount + rate) * 100) / 100

    fun snapshot(): DeclarationBaseline {
        return DeclarationBaseline(
            code = code,
            name = name,
            amount = amount,
            rate = rate,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration baseline in status draft. */
fun newDeclarationBaseline(code: String, name: String): DeclarationBaseline {
    return DeclarationBaseline(
        code = code,
        name = name,
        amount = 0.0,
        rate = 0.0,
        shiftCode = "",
        status = DeclarationBaselineStatus.DRAFT,
    )
}
