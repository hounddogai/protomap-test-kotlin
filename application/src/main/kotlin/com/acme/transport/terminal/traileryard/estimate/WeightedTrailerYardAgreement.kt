package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted trailer yard agreement is in its life cycle. */
enum class WeightedTrailerYardAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted trailer yard agreement. Its code is unique within a tenant. */
data class WeightedTrailerYardAgreement(
    var code: String,
    var label: String,
    var rate: Double,
    var tags: MutableList<String>,
    var locale: String,
    var notes: String,
    var approved: Boolean,
    var priority: Int,
    var status: WeightedTrailerYardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerYardAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerYardAgreementStatus): Boolean {
        when (status) {
            WeightedTrailerYardAgreementStatus.PLANNED -> return next == WeightedTrailerYardAgreementStatus.SCHEDULED || next == WeightedTrailerYardAgreementStatus.CANCELLED
            WeightedTrailerYardAgreementStatus.SCHEDULED -> return next == WeightedTrailerYardAgreementStatus.COMPLETED || next == WeightedTrailerYardAgreementStatus.CANCELLED
            WeightedTrailerYardAgreementStatus.IN_PROGRESS -> return next == WeightedTrailerYardAgreementStatus.COMPLETED || next == WeightedTrailerYardAgreementStatus.CANCELLED
            WeightedTrailerYardAgreementStatus.COMPLETED -> {
                when (next) {
                    WeightedTrailerYardAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTrailerYardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer yard agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(rate * priority.toDouble() * 100) / 100

    fun snapshot(): WeightedTrailerYardAgreement {
        return WeightedTrailerYardAgreement(
            code = code,
            label = label,
            rate = rate,
            tags = tags,
            locale = locale,
            notes = notes,
            approved = approved,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted trailer yard agreement in status planned. */
fun newWeightedTrailerYardAgreement(code: String, label: String): WeightedTrailerYardAgreement {
    return WeightedTrailerYardAgreement(
        code = code,
        label = label,
        rate = 0.0,
        tags = mutableListOf<String>(),
        locale = "",
        notes = "",
        approved = false,
        priority = 0,
        status = WeightedTrailerYardAgreementStatus.PLANNED,
    )
}
