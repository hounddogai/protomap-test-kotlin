package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered declaration baseline is in its life cycle. */
enum class TieredDeclarationBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered declaration baseline as the declaration team keeps it. */
data class TieredDeclarationBaseline(
    var code: String,
    var label: String,
    var shiftCode: String,
    var summary: String,
    var currency: String,
    var sequence: Int,
    var rate: Double,
    var description: String,
    var graceDays: Int,
    var timezone: String,
    var minimum: Double,
    var status: TieredDeclarationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationBaselineStatus): Boolean {
        when (status) {
            TieredDeclarationBaselineStatus.PROPOSED -> return next == TieredDeclarationBaselineStatus.APPROVED || next == TieredDeclarationBaselineStatus.WITHDRAWN
            TieredDeclarationBaselineStatus.APPROVED -> return next == TieredDeclarationBaselineStatus.REJECTED
            TieredDeclarationBaselineStatus.REJECTED -> {
                when (next) {
                    TieredDeclarationBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDeclarationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round((sequence.toDouble() + minimum) * 100) / 100

    fun snapshot(): TieredDeclarationBaseline {
        return TieredDeclarationBaseline(
            code = code,
            label = label,
            shiftCode = shiftCode,
            summary = summary,
            currency = currency,
            sequence = sequence,
            rate = rate,
            description = description,
            graceDays = graceDays,
            timezone = timezone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered declaration baseline in status proposed. */
fun newTieredDeclarationBaseline(code: String, label: String): TieredDeclarationBaseline {
    return TieredDeclarationBaseline(
        code = code,
        label = label,
        shiftCode = "",
        summary = "",
        currency = "",
        sequence = 0,
        rate = 0.0,
        description = "",
        graceDays = 0,
        timezone = "",
        minimum = 0.0,
        status = TieredDeclarationBaselineStatus.PROPOSED,
    )
}
