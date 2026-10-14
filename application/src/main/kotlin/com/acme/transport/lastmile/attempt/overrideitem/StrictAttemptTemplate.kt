package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict attempt template is in its life cycle. */
enum class StrictAttemptTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a strict attempt template and its current status. */
data class StrictAttemptTemplate(
    var code: String,
    var title: String,
    var locale: String,
    var score: Double,
    var startsAt: Int,
    var cost: Double,
    var currency: String,
    var weight: Double,
    var fuelLiters: Double,
    var status: StrictAttemptTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAttemptTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAttemptTemplateStatus): Boolean {
        when (status) {
            StrictAttemptTemplateStatus.CREATED -> return next == StrictAttemptTemplateStatus.PUBLISHED
            StrictAttemptTemplateStatus.VALIDATED -> return next == StrictAttemptTemplateStatus.PUBLISHED
            StrictAttemptTemplateStatus.PUBLISHED -> {
                when (next) {
                    StrictAttemptTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAttemptTemplateStatus) {
        if (!canMoveTo(next)) {
            error("strict attempt template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((fuelLiters + startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictAttemptTemplate {
        return StrictAttemptTemplate(
            code = code,
            title = title,
            locale = locale,
            score = score,
            startsAt = startsAt,
            cost = cost,
            currency = currency,
            weight = weight,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict attempt template in status created. */
fun newStrictAttemptTemplate(code: String, title: String): StrictAttemptTemplate {
    return StrictAttemptTemplate(
        code = code,
        title = title,
        locale = "",
        score = 0.0,
        startsAt = 0,
        cost = 0.0,
        currency = "",
        weight = 0.0,
        fuelLiters = 0.0,
        status = StrictAttemptTemplateStatus.CREATED,
    )
}
