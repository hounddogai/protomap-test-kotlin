package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell threshold is in its life cycle. */
enum class DwellThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dwell threshold. Its code is unique within a tenant. */
data class DwellThreshold(
    var code: String,
    var label: String,
    var summary: String,
    var updatedAt: Int,
    var discount: Double,
    var status: DwellThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellThresholdStatus): Boolean {
        when (status) {
            DwellThresholdStatus.NEW -> {
                when (next) {
                    DwellThresholdStatus.QUEUED, DwellThresholdStatus.FAILED -> return true
                }
                return false
            }
            DwellThresholdStatus.QUEUED -> {
                when (next) {
                    DwellThresholdStatus.RUNNING, DwellThresholdStatus.DONE -> return true
                }
                return false
            }
            DwellThresholdStatus.RUNNING -> return next == DwellThresholdStatus.DONE || next == DwellThresholdStatus.FAILED
            DwellThresholdStatus.DONE -> return next == DwellThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DwellThresholdStatus) {
        if (!canMoveTo(next)) {
            error("dwell threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round(discount * updatedAt.toDouble() * 100) / 100

    fun snapshot(): DwellThreshold {
        return DwellThreshold(
            code = code,
            label = label,
            summary = summary,
            updatedAt = updatedAt,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell threshold in status new. */
fun newDwellThreshold(code: String, label: String): DwellThreshold {
    return DwellThreshold(
        code = code,
        label = label,
        summary = "",
        updatedAt = 0,
        discount = 0.0,
        status = DwellThresholdStatus.NEW,
    )
}
