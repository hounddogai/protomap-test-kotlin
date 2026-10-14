package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted tender exception records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a weighted tender exception is in its life cycle. */
enum class WeightedTenderExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted tender exception, with what the planners need to act on it. */
data class WeightedTenderException(
    var code: String,
    var name: String,
    var description: String,
    var locale: String,
    var loadKg: Double,
    var approved: Boolean,
    var cost: Double,
    var expiresAt: Int,
    var status: WeightedTenderExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTenderExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTenderExceptionStatus): Boolean {
        when (status) {
            WeightedTenderExceptionStatus.CREATED -> {
                when (next) {
                    WeightedTenderExceptionStatus.PUBLISHED -> return true
                }
                return false
            }
            WeightedTenderExceptionStatus.VALIDATED -> {
                when (next) {
                    WeightedTenderExceptionStatus.PUBLISHED, WeightedTenderExceptionStatus.RETIRED -> return true
                }
                return false
            }
            WeightedTenderExceptionStatus.PUBLISHED -> return next == WeightedTenderExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedTenderExceptionStatus) {
        if (!canMoveTo(next)) {
            error("weighted tender exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(cost * loadKg * 100) / 100

    fun snapshot(): WeightedTenderException {
        return WeightedTenderException(
            code = code,
            name = name,
            description = description,
            locale = locale,
            loadKg = loadKg,
            approved = approved,
            cost = cost,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted tender exception in status created. */
fun newWeightedTenderException(code: String, name: String): WeightedTenderException {
    return WeightedTenderException(
        code = code,
        name = name,
        description = "",
        locale = "",
        loadKg = 0.0,
        approved = false,
        cost = 0.0,
        expiresAt = 0,
        status = WeightedTenderExceptionStatus.CREATED,
    )
}
