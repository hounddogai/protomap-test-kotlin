package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default ev charge score records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a default ev charge score is in its life cycle. */
enum class DefaultEvChargeScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default ev charge score. Its code is unique within a tenant. */
data class DefaultEvChargeScore(
    var code: String,
    var label: String,
    var ceiling: Double,
    var discount: Double,
    var shiftCode: String,
    var locale: String,
    var fuelLiters: Double,
    var approved: Boolean,
    var status: DefaultEvChargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEvChargeScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEvChargeScoreStatus): Boolean {
        when (status) {
            DefaultEvChargeScoreStatus.CREATED -> {
                when (next) {
                    DefaultEvChargeScoreStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultEvChargeScoreStatus.VALIDATED -> {
                when (next) {
                    DefaultEvChargeScoreStatus.PUBLISHED, DefaultEvChargeScoreStatus.RETIRED -> return true
                }
                return false
            }
            DefaultEvChargeScoreStatus.PUBLISHED -> {
                when (next) {
                    DefaultEvChargeScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultEvChargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("default ev charge score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default ev charge score"} $code"

    fun adjustedExposure(): Double = round((ceiling + discount) * 100) / 100

    fun snapshot(): DefaultEvChargeScore {
        return DefaultEvChargeScore(
            code = code,
            label = label,
            ceiling = ceiling,
            discount = discount,
            shiftCode = shiftCode,
            locale = locale,
            fuelLiters = fuelLiters,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default ev charge score in status created. */
fun newDefaultEvChargeScore(code: String, label: String): DefaultEvChargeScore {
    return DefaultEvChargeScore(
        code = code,
        label = label,
        ceiling = 0.0,
        discount = 0.0,
        shiftCode = "",
        locale = "",
        fuelLiters = 0.0,
        approved = false,
        status = DefaultEvChargeScoreStatus.CREATED,
    )
}
