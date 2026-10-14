package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell override is in its life cycle. */
enum class DwellOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell override, with what the planners need to act on it. */
data class DwellOverride(
    var code: String,
    var title: String,
    var driverId: String,
    var etaMinutes: Int,
    var budget: Double,
    var status: DwellOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellOverrideStatus): Boolean {
        when (status) {
            DwellOverrideStatus.DRAFT -> return next == DwellOverrideStatus.PENDING || next == DwellOverrideStatus.ACTIVE
            DwellOverrideStatus.PENDING -> {
                when (next) {
                    DwellOverrideStatus.ACTIVE -> return true
                }
                return false
            }
            DwellOverrideStatus.ACTIVE -> {
                when (next) {
                    DwellOverrideStatus.SUSPENDED, DwellOverrideStatus.CLOSED -> return true
                }
                return false
            }
            DwellOverrideStatus.SUSPENDED -> {
                when (next) {
                    DwellOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellOverrideStatus) {
        if (!canMoveTo(next)) {
            error("dwell override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((budget - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DwellOverride {
        return DwellOverride(
            code = code,
            title = title,
            driverId = driverId,
            etaMinutes = etaMinutes,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell override in status draft. */
fun newDwellOverride(code: String, title: String): DwellOverride {
    return DwellOverride(
        code = code,
        title = title,
        driverId = "",
        etaMinutes = 0,
        budget = 0.0,
        status = DwellOverrideStatus.DRAFT,
    )
}
