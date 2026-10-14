package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rate card target is in its life cycle. */
enum class StrictRateCardTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict rate card target. Its code is unique within a tenant. */
data class StrictRateCardTarget(
    var code: String,
    var title: String,
    var dock: String,
    var ownerId: String,
    var floorValue: Double,
    var externalRef: String,
    var limit: Int,
    var status: StrictRateCardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardTargetStatus): Boolean {
        when (status) {
            StrictRateCardTargetStatus.CREATED -> return next == StrictRateCardTargetStatus.PUBLISHED
            StrictRateCardTargetStatus.VALIDATED -> {
                when (next) {
                    StrictRateCardTargetStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictRateCardTargetStatus.PUBLISHED -> return next == StrictRateCardTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictRateCardTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((limit.toDouble() + floorValue) * 100) / 100

    fun snapshot(): StrictRateCardTarget {
        return StrictRateCardTarget(
            code = code,
            title = title,
            dock = dock,
            ownerId = ownerId,
            floorValue = floorValue,
            externalRef = externalRef,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict rate card target in status created. */
fun newStrictRateCardTarget(code: String, title: String): StrictRateCardTarget {
    return StrictRateCardTarget(
        code = code,
        title = title,
        dock = "",
        ownerId = "",
        floorValue = 0.0,
        externalRef = "",
        limit = 0,
        status = StrictRateCardTargetStatus.CREATED,
    )
}
