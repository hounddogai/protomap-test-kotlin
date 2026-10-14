package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize

/** Where a weighted drop exception is in its life cycle. */
enum class WeightedDropExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a weighted drop exception and its current status. */
data class WeightedDropException(
    var code: String,
    var title: String,
    var amount: Double,
    var active: Boolean,
    var contactEmail: String,
    var status: WeightedDropExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDropExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDropExceptionStatus): Boolean {
        when (status) {
            WeightedDropExceptionStatus.REQUESTED -> return next == WeightedDropExceptionStatus.GRANTED || next == WeightedDropExceptionStatus.REVOKED
            WeightedDropExceptionStatus.GRANTED -> return next == WeightedDropExceptionStatus.REVOKED || next == WeightedDropExceptionStatus.EXPIRED
            WeightedDropExceptionStatus.REVOKED -> {
                when (next) {
                    WeightedDropExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDropExceptionStatus) {
        if (!canMoveTo(next)) {
            error("weighted drop exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted drop exception"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): WeightedDropException {
        return WeightedDropException(
            code = code,
            title = title,
            amount = amount,
            active = active,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted drop exception in status requested. */
fun newWeightedDropException(code: String, title: String): WeightedDropException {
    return WeightedDropException(
        code = code,
        title = title,
        amount = 0.0,
        active = false,
        contactEmail = "",
        status = WeightedDropExceptionStatus.REQUESTED,
    )
}
