package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard agreement is in its life cycle. */
enum class TrailerYardAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a trailer yard agreement and its current status. */
data class TrailerYardAgreement(
    var code: String,
    var title: String,
    var ratio: Double,
    var limit: Int,
    var quantity: Int,
    var phone: String,
    var status: TrailerYardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardAgreementStatus): Boolean {
        when (status) {
            TrailerYardAgreementStatus.PLANNED -> {
                when (next) {
                    TrailerYardAgreementStatus.SCHEDULED, TrailerYardAgreementStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TrailerYardAgreementStatus.SCHEDULED -> return next == TrailerYardAgreementStatus.IN_PROGRESS || next == TrailerYardAgreementStatus.CANCELLED
            TrailerYardAgreementStatus.IN_PROGRESS -> return next == TrailerYardAgreementStatus.COMPLETED || next == TrailerYardAgreementStatus.CANCELLED
            TrailerYardAgreementStatus.COMPLETED -> return next == TrailerYardAgreementStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerYardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((limit.toDouble() + ratio) * 100) / 100

    fun snapshot(): TrailerYardAgreement {
        return TrailerYardAgreement(
            code = code,
            title = title,
            ratio = ratio,
            limit = limit,
            quantity = quantity,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard agreement in status planned. */
fun newTrailerYardAgreement(code: String, title: String): TrailerYardAgreement {
    return TrailerYardAgreement(
        code = code,
        title = title,
        ratio = 0.0,
        limit = 0,
        quantity = 0,
        phone = "",
        status = TrailerYardAgreementStatus.PLANNED,
    )
}
