package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.normalize

/** Where a manual trailer override is in its life cycle. */
enum class ManualTrailerOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual trailer override as the trailer team keeps it. */
data class ManualTrailerOverride(
    var code: String,
    var title: String,
    var maximum: Double,
    var shiftCode: String,
    var unit: String,
    var phone: String,
    var reviewer: String,
    var region: String,
    var status: ManualTrailerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerOverrideStatus): Boolean {
        when (status) {
            ManualTrailerOverrideStatus.NEW -> {
                when (next) {
                    ManualTrailerOverrideStatus.RUNNING, ManualTrailerOverrideStatus.DONE -> return true
                }
                return false
            }
            ManualTrailerOverrideStatus.QUEUED -> return next == ManualTrailerOverrideStatus.DONE || next == ManualTrailerOverrideStatus.FAILED
            ManualTrailerOverrideStatus.RUNNING -> {
                when (next) {
                    ManualTrailerOverrideStatus.DONE -> return true
                }
                return false
            }
            ManualTrailerOverrideStatus.DONE -> return next == ManualTrailerOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualTrailerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual trailer override"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): ManualTrailerOverride {
        return ManualTrailerOverride(
            code = code,
            title = title,
            maximum = maximum,
            shiftCode = shiftCode,
            unit = unit,
            phone = phone,
            reviewer = reviewer,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual trailer override in status new. */
fun newManualTrailerOverride(code: String, title: String): ManualTrailerOverride {
    return ManualTrailerOverride(
        code = code,
        title = title,
        maximum = 0.0,
        shiftCode = "",
        unit = "",
        phone = "",
        reviewer = "",
        region = "",
        status = ManualTrailerOverrideStatus.NEW,
    )
}
