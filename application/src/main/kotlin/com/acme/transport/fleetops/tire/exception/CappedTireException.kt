package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped tire exception records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a capped tire exception is in its life cycle. */
enum class CappedTireExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped tire exception, with what the planners need to act on it. */
data class CappedTireException(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var description: String,
    var discount: Double,
    var cost: Double,
    var limit: Int,
    var country: String,
    var capacity: Int,
    var status: CappedTireExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTireExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTireExceptionStatus): Boolean {
        when (status) {
            CappedTireExceptionStatus.DRAFT -> {
                when (next) {
                    CappedTireExceptionStatus.PENDING, CappedTireExceptionStatus.CLOSED -> return true
                }
                return false
            }
            CappedTireExceptionStatus.PENDING -> return next == CappedTireExceptionStatus.CLOSED
            CappedTireExceptionStatus.ACTIVE -> {
                when (next) {
                    CappedTireExceptionStatus.SUSPENDED, CappedTireExceptionStatus.CLOSED -> return true
                }
                return false
            }
            CappedTireExceptionStatus.SUSPENDED -> {
                when (next) {
                    CappedTireExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTireExceptionStatus) {
        if (!canMoveTo(next)) {
            error("capped tire exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((discount - limit.toDouble()) * 100) / 100

    fun snapshot(): CappedTireException {
        return CappedTireException(
            code = code,
            label = label,
            tags = tags,
            description = description,
            discount = discount,
            cost = cost,
            limit = limit,
            country = country,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped tire exception in status draft. */
fun newCappedTireException(code: String, label: String): CappedTireException {
    return CappedTireException(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        description = "",
        discount = 0.0,
        cost = 0.0,
        limit = 0,
        country = "",
        capacity = 0,
        status = CappedTireExceptionStatus.DRAFT,
    )
}
