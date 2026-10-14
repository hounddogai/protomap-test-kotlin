package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict backhaul baseline is in its life cycle. */
enum class StrictBackhaulBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict backhaul baseline. Its code is unique within a tenant. */
data class StrictBackhaulBaseline(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var externalRef: String,
    var expiresAt: Int,
    var rate: Double,
    var status: StrictBackhaulBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulBaselineStatus): Boolean {
        when (status) {
            StrictBackhaulBaselineStatus.CREATED -> return next == StrictBackhaulBaselineStatus.VALIDATED || next == StrictBackhaulBaselineStatus.RETIRED
            StrictBackhaulBaselineStatus.VALIDATED -> return next == StrictBackhaulBaselineStatus.PUBLISHED || next == StrictBackhaulBaselineStatus.RETIRED
            StrictBackhaulBaselineStatus.PUBLISHED -> {
                when (next) {
                    StrictBackhaulBaselineStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictBackhaulBaselineStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round((expiresAt.toDouble() - rate) * 100) / 100

    fun snapshot(): StrictBackhaulBaseline {
        return StrictBackhaulBaseline(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            externalRef = externalRef,
            expiresAt = expiresAt,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict backhaul baseline in status created. */
fun newStrictBackhaulBaseline(code: String, label: String): StrictBackhaulBaseline {
    return StrictBackhaulBaseline(
        code = code,
        label = label,
        durationMinutes = 0,
        externalRef = "",
        expiresAt = 0,
        rate = 0.0,
        status = StrictBackhaulBaselineStatus.CREATED,
    )
}
