package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt baseline is in its life cycle. */
enum class AttemptBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a attempt baseline and its current status. */
data class AttemptBaseline(
    var code: String,
    var title: String,
    var depot: String,
    var amount: Double,
    var segment: String,
    var name: String,
    var maximum: Double,
    var status: AttemptBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptBaselineStatus): Boolean {
        when (status) {
            AttemptBaselineStatus.CREATED -> {
                when (next) {
                    AttemptBaselineStatus.PUBLISHED, AttemptBaselineStatus.RETIRED -> return true
                }
                return false
            }
            AttemptBaselineStatus.VALIDATED -> return next == AttemptBaselineStatus.PUBLISHED || next == AttemptBaselineStatus.RETIRED
            AttemptBaselineStatus.PUBLISHED -> return next == AttemptBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AttemptBaselineStatus) {
        if (!canMoveTo(next)) {
            error("attempt baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round(maximum * amount * 100) / 100

    fun snapshot(): AttemptBaseline {
        return AttemptBaseline(
            code = code,
            title = title,
            depot = depot,
            amount = amount,
            segment = segment,
            name = name,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a attempt baseline in status created. */
fun newAttemptBaseline(code: String, title: String): AttemptBaseline {
    return AttemptBaseline(
        code = code,
        title = title,
        depot = "",
        amount = 0.0,
        segment = "",
        name = "",
        maximum = 0.0,
        status = AttemptBaselineStatus.CREATED,
    )
}
