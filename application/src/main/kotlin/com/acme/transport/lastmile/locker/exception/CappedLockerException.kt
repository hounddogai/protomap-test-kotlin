package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped locker exception is in its life cycle. */
enum class CappedLockerExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped locker exception and its current status. */
data class CappedLockerException(
    var code: String,
    var label: String,
    var quantity: Int,
    var tags: MutableList<String>,
    var summary: String,
    var title: String,
    var maximum: Double,
    var rate: Double,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var discount: Double,
    var status: CappedLockerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerExceptionStatus): Boolean {
        when (status) {
            CappedLockerExceptionStatus.PLANNED -> {
                when (next) {
                    CappedLockerExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            CappedLockerExceptionStatus.SCHEDULED -> {
                when (next) {
                    CappedLockerExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            CappedLockerExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    CappedLockerExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            CappedLockerExceptionStatus.COMPLETED -> return next == CappedLockerExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedLockerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("capped locker exception $code cannot move from $status to $next")
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

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((discount + rate) * 100) / 100

    fun snapshot(): CappedLockerException {
        return CappedLockerException(
            code = code,
            label = label,
            quantity = quantity,
            tags = tags,
            summary = summary,
            title = title,
            maximum = maximum,
            rate = rate,
            depot = depot,
            attributes = attributes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped locker exception in status planned. */
fun newCappedLockerException(code: String, label: String): CappedLockerException {
    return CappedLockerException(
        code = code,
        label = label,
        quantity = 0,
        tags = mutableListOf<String>(),
        summary = "",
        title = "",
        maximum = 0.0,
        rate = 0.0,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        discount = 0.0,
        status = CappedLockerExceptionStatus.PLANNED,
    )
}
