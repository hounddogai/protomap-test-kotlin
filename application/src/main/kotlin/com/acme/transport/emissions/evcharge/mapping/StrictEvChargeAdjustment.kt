package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict ev charge adjustment is in its life cycle. */
enum class StrictEvChargeAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict ev charge adjustment as the ev charge team keeps it. */
data class StrictEvChargeAdjustment(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var category: String,
    var summary: String,
    var amount: Double,
    var graceDays: Int,
    var startsAt: Int,
    var status: StrictEvChargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeAdjustmentStatus): Boolean {
        when (status) {
            StrictEvChargeAdjustmentStatus.DRAFT -> {
                when (next) {
                    StrictEvChargeAdjustmentStatus.ACTIVE -> return true
                }
                return false
            }
            StrictEvChargeAdjustmentStatus.PENDING -> return next == StrictEvChargeAdjustmentStatus.SUSPENDED
            StrictEvChargeAdjustmentStatus.ACTIVE -> {
                when (next) {
                    StrictEvChargeAdjustmentStatus.SUSPENDED, StrictEvChargeAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            StrictEvChargeAdjustmentStatus.SUSPENDED -> {
                when (next) {
                    StrictEvChargeAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictEvChargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((graceDays.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictEvChargeAdjustment {
        return StrictEvChargeAdjustment(
            code = code,
            label = label,
            attributes = attributes,
            category = category,
            summary = summary,
            amount = amount,
            graceDays = graceDays,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict ev charge adjustment in status draft. */
fun newStrictEvChargeAdjustment(code: String, label: String): StrictEvChargeAdjustment {
    return StrictEvChargeAdjustment(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        category = "",
        summary = "",
        amount = 0.0,
        graceDays = 0,
        startsAt = 0,
        status = StrictEvChargeAdjustmentStatus.DRAFT,
    )
}
