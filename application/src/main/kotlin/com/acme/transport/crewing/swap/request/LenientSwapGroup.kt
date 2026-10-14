package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient swap group is in its life cycle. */
enum class LenientSwapGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient swap group. Its code is unique within a tenant. */
data class LenientSwapGroup(
    var code: String,
    var label: String,
    var description: String,
    var updatedAt: Int,
    var minimum: Double,
    var graceDays: Int,
    var createdAt: Int,
    var locale: String,
    var pallets: Int,
    var maximum: Double,
    var segment: String,
    var status: LenientSwapGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientSwapGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientSwapGroupStatus): Boolean {
        when (status) {
            LenientSwapGroupStatus.DRAFT -> return next == LenientSwapGroupStatus.ACTIVE || next == LenientSwapGroupStatus.CLOSED
            LenientSwapGroupStatus.PENDING -> return next == LenientSwapGroupStatus.ACTIVE
            LenientSwapGroupStatus.ACTIVE -> {
                when (next) {
                    LenientSwapGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientSwapGroupStatus.SUSPENDED -> return next == LenientSwapGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientSwapGroupStatus) {
        if (!canMoveTo(next)) {
            error("lenient swap group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round(minimum * maximum * 100) / 100

    fun snapshot(): LenientSwapGroup {
        return LenientSwapGroup(
            code = code,
            label = label,
            description = description,
            updatedAt = updatedAt,
            minimum = minimum,
            graceDays = graceDays,
            createdAt = createdAt,
            locale = locale,
            pallets = pallets,
            maximum = maximum,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient swap group in status draft. */
fun newLenientSwapGroup(code: String, label: String): LenientSwapGroup {
    return LenientSwapGroup(
        code = code,
        label = label,
        description = "",
        updatedAt = 0,
        minimum = 0.0,
        graceDays = 0,
        createdAt = 0,
        locale = "",
        pallets = 0,
        maximum = 0.0,
        segment = "",
        status = LenientSwapGroupStatus.DRAFT,
    )
}
