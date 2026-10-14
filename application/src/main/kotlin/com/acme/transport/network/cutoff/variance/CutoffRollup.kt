package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff rollup is in its life cycle. */
enum class CutoffRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff rollup as the cutoff team keeps it. */
data class CutoffRollup(
    var code: String,
    var name: String,
    var pallets: Int,
    var etaMinutes: Int,
    var retries: Int,
    var label: String,
    var priority: Int,
    var driverId: String,
    var status: CutoffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffRollupStatus): Boolean {
        when (status) {
            CutoffRollupStatus.NEW -> {
                when (next) {
                    CutoffRollupStatus.QUEUED -> return true
                }
                return false
            }
            CutoffRollupStatus.QUEUED -> return next == CutoffRollupStatus.DONE || next == CutoffRollupStatus.FAILED
            CutoffRollupStatus.RUNNING -> {
                when (next) {
                    CutoffRollupStatus.DONE, CutoffRollupStatus.FAILED -> return true
                }
                return false
            }
            CutoffRollupStatus.DONE -> return next == CutoffRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffRollupStatus) {
        if (!canMoveTo(next)) {
            error("cutoff rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cutoff rollup"} $code"

    fun netValue(): Double = round(etaMinutes.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): CutoffRollup {
        return CutoffRollup(
            code = code,
            name = name,
            pallets = pallets,
            etaMinutes = etaMinutes,
            retries = retries,
            label = label,
            priority = priority,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff rollup in status new. */
fun newCutoffRollup(code: String, name: String): CutoffRollup {
    return CutoffRollup(
        code = code,
        name = name,
        pallets = 0,
        etaMinutes = 0,
        retries = 0,
        label = "",
        priority = 0,
        driverId = "",
        status = CutoffRollupStatus.NEW,
    )
}
