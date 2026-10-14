package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect rollup is in its life cycle. */
enum class DefectRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A defect rollup. Its code is unique within a tenant. */
data class DefectRollup(
    var code: String,
    var name: String,
    var capacity: Int,
    var unit: String,
    var stopCount: Int,
    var status: DefectRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectRollupStatus): Boolean {
        when (status) {
            DefectRollupStatus.REQUESTED -> return next == DefectRollupStatus.GRANTED || next == DefectRollupStatus.REVOKED
            DefectRollupStatus.GRANTED -> {
                when (next) {
                    DefectRollupStatus.REVOKED, DefectRollupStatus.EXPIRED -> return true
                }
                return false
            }
            DefectRollupStatus.REVOKED -> {
                when (next) {
                    DefectRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectRollupStatus) {
        if (!canMoveTo(next)) {
            error("defect rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round((stopCount.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): DefectRollup {
        return DefectRollup(
            code = code,
            name = name,
            capacity = capacity,
            unit = unit,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a defect rollup in status requested. */
fun newDefectRollup(code: String, name: String): DefectRollup {
    return DefectRollup(
        code = code,
        name = name,
        capacity = 0,
        unit = "",
        stopCount = 0,
        status = DefectRollupStatus.REQUESTED,
    )
}
