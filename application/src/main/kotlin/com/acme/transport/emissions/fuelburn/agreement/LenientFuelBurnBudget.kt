package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel burn budget is in its life cycle. */
enum class LenientFuelBurnBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient fuel burn budget as the fuel burn team keeps it. */
data class LenientFuelBurnBudget(
    var code: String,
    var name: String,
    var sequence: Int,
    var title: String,
    var expiresAt: Int,
    var ownerId: String,
    var externalRef: String,
    var limit: Int,
    var weight: Double,
    var checksum: String,
    var endsAt: Int,
    var status: LenientFuelBurnBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelBurnBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelBurnBudgetStatus): Boolean {
        when (status) {
            LenientFuelBurnBudgetStatus.REQUESTED -> {
                when (next) {
                    LenientFuelBurnBudgetStatus.REVOKED, LenientFuelBurnBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            LenientFuelBurnBudgetStatus.GRANTED -> return next == LenientFuelBurnBudgetStatus.EXPIRED
            LenientFuelBurnBudgetStatus.REVOKED -> return next == LenientFuelBurnBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientFuelBurnBudgetStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel burn budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round(weight * expiresAt.toDouble() * 100) / 100

    fun snapshot(): LenientFuelBurnBudget {
        return LenientFuelBurnBudget(
            code = code,
            name = name,
            sequence = sequence,
            title = title,
            expiresAt = expiresAt,
            ownerId = ownerId,
            externalRef = externalRef,
            limit = limit,
            weight = weight,
            checksum = checksum,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient fuel burn budget in status requested. */
fun newLenientFuelBurnBudget(code: String, name: String): LenientFuelBurnBudget {
    return LenientFuelBurnBudget(
        code = code,
        name = name,
        sequence = 0,
        title = "",
        expiresAt = 0,
        ownerId = "",
        externalRef = "",
        limit = 0,
        weight = 0.0,
        checksum = "",
        endsAt = 0,
        status = LenientFuelBurnBudgetStatus.REQUESTED,
    )
}
