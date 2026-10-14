package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff group is in its life cycle. */
enum class CutoffGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One cutoff group, with what the planners need to act on it. */
data class CutoffGroup(
    var code: String,
    var label: String,
    var batchSize: Int,
    var priority: Int,
    var margin: Double,
    var rate: Double,
    var name: String,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var status: CutoffGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffGroupStatus): Boolean {
        when (status) {
            CutoffGroupStatus.DRAFT -> {
                when (next) {
                    CutoffGroupStatus.PENDING, CutoffGroupStatus.CLOSED -> return true
                }
                return false
            }
            CutoffGroupStatus.PENDING -> return next == CutoffGroupStatus.SUSPENDED || next == CutoffGroupStatus.CLOSED
            CutoffGroupStatus.ACTIVE -> return next == CutoffGroupStatus.SUSPENDED
            CutoffGroupStatus.SUSPENDED -> {
                when (next) {
                    CutoffGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffGroupStatus) {
        if (!canMoveTo(next)) {
            error("cutoff group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round((etaMinutes.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): CutoffGroup {
        return CutoffGroup(
            code = code,
            label = label,
            batchSize = batchSize,
            priority = priority,
            margin = margin,
            rate = rate,
            name = name,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff group in status draft. */
fun newCutoffGroup(code: String, label: String): CutoffGroup {
    return CutoffGroup(
        code = code,
        label = label,
        batchSize = 0,
        priority = 0,
        margin = 0.0,
        rate = 0.0,
        name = "",
        fuelLiters = 0.0,
        etaMinutes = 0,
        status = CutoffGroupStatus.DRAFT,
    )
}
