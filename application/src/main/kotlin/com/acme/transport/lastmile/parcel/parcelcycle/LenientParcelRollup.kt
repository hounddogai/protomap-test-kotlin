package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient parcel rollup is in its life cycle. */
enum class LenientParcelRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient parcel rollup and its current status. */
data class LenientParcelRollup(
    var code: String,
    var name: String,
    var description: String,
    var revision: Int,
    var minimum: Double,
    var amount: Double,
    var startsAt: Int,
    var cost: Double,
    var graceDays: Int,
    var durationMinutes: Int,
    var approved: Boolean,
    var status: LenientParcelRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientParcelRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientParcelRollupStatus): Boolean {
        when (status) {
            LenientParcelRollupStatus.DRAFT -> {
                when (next) {
                    LenientParcelRollupStatus.CLOSED -> return true
                }
                return false
            }
            LenientParcelRollupStatus.PENDING -> {
                when (next) {
                    LenientParcelRollupStatus.CLOSED -> return true
                }
                return false
            }
            LenientParcelRollupStatus.ACTIVE -> return next == LenientParcelRollupStatus.CLOSED
            LenientParcelRollupStatus.SUSPENDED -> return next == LenientParcelRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientParcelRollupStatus) {
        if (!canMoveTo(next)) {
            error("lenient parcel rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"lenient parcel rollup"} $code"

    fun adjustedValue(): Double = round(revision.toDouble() * cost * 100) / 100

    fun snapshot(): LenientParcelRollup {
        return LenientParcelRollup(
            code = code,
            name = name,
            description = description,
            revision = revision,
            minimum = minimum,
            amount = amount,
            startsAt = startsAt,
            cost = cost,
            graceDays = graceDays,
            durationMinutes = durationMinutes,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient parcel rollup in status draft. */
fun newLenientParcelRollup(code: String, name: String): LenientParcelRollup {
    return LenientParcelRollup(
        code = code,
        name = name,
        description = "",
        revision = 0,
        minimum = 0.0,
        amount = 0.0,
        startsAt = 0,
        cost = 0.0,
        graceDays = 0,
        durationMinutes = 0,
        approved = false,
        status = LenientParcelRollupStatus.DRAFT,
    )
}
