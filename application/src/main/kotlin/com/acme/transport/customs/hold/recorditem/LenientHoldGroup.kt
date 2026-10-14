package com.acme.transport.customs.hold.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient hold group is in its life cycle. */
enum class LenientHoldGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient hold group, with what the planners need to act on it. */
data class LenientHoldGroup(
    var code: String,
    var label: String,
    var version: Int,
    var country: String,
    var margin: Double,
    var ratio: Double,
    var updatedAt: Int,
    var unit: String,
    var status: LenientHoldGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHoldGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHoldGroupStatus): Boolean {
        when (status) {
            LenientHoldGroupStatus.CREATED -> {
                when (next) {
                    LenientHoldGroupStatus.PUBLISHED, LenientHoldGroupStatus.RETIRED -> return true
                }
                return false
            }
            LenientHoldGroupStatus.VALIDATED -> return next == LenientHoldGroupStatus.PUBLISHED || next == LenientHoldGroupStatus.RETIRED
            LenientHoldGroupStatus.PUBLISHED -> return next == LenientHoldGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientHoldGroupStatus) {
        if (!canMoveTo(next)) {
            error("lenient hold group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((margin + version.toDouble()) * 100) / 100

    fun snapshot(): LenientHoldGroup {
        return LenientHoldGroup(
            code = code,
            label = label,
            version = version,
            country = country,
            margin = margin,
            ratio = ratio,
            updatedAt = updatedAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient hold group in status created. */
fun newLenientHoldGroup(code: String, label: String): LenientHoldGroup {
    return LenientHoldGroup(
        code = code,
        label = label,
        version = 0,
        country = "",
        margin = 0.0,
        ratio = 0.0,
        updatedAt = 0,
        unit = "",
        status = LenientHoldGroupStatus.CREATED,
    )
}
