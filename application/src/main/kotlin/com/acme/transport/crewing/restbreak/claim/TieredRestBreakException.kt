package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered rest break exception is in its life cycle. */
enum class TieredRestBreakExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered rest break exception. Its code is unique within a tenant. */
data class TieredRestBreakException(
    var code: String,
    var label: String,
    var minimum: Double,
    var active: Boolean,
    var title: String,
    var maximum: Double,
    var fuelLiters: Double,
    var ceiling: Double,
    var capacity: Int,
    var status: TieredRestBreakExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRestBreakExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRestBreakExceptionStatus): Boolean {
        when (status) {
            TieredRestBreakExceptionStatus.CREATED -> return next == TieredRestBreakExceptionStatus.PUBLISHED
            TieredRestBreakExceptionStatus.VALIDATED -> return next == TieredRestBreakExceptionStatus.PUBLISHED || next == TieredRestBreakExceptionStatus.RETIRED
            TieredRestBreakExceptionStatus.PUBLISHED -> {
                when (next) {
                    TieredRestBreakExceptionStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRestBreakExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tiered rest break exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedValue(): Double = round((capacity.toDouble() + ceiling) * 100) / 100

    fun snapshot(): TieredRestBreakException {
        return TieredRestBreakException(
            code = code,
            label = label,
            minimum = minimum,
            active = active,
            title = title,
            maximum = maximum,
            fuelLiters = fuelLiters,
            ceiling = ceiling,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered rest break exception in status created. */
fun newTieredRestBreakException(code: String, label: String): TieredRestBreakException {
    return TieredRestBreakException(
        code = code,
        label = label,
        minimum = 0.0,
        active = false,
        title = "",
        maximum = 0.0,
        fuelLiters = 0.0,
        ceiling = 0.0,
        capacity = 0,
        status = TieredRestBreakExceptionStatus.CREATED,
    )
}
