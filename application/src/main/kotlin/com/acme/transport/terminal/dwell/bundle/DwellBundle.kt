package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell bundle records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell bundle is in its life cycle. */
enum class DwellBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dwell bundle. Its code is unique within a tenant. */
data class DwellBundle(
    var code: String,
    var label: String,
    var revision: Int,
    var approved: Boolean,
    var threshold: Double,
    var sequence: Int,
    var status: DwellBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellBundleStatus): Boolean {
        when (status) {
            DwellBundleStatus.PLANNED -> return next == DwellBundleStatus.COMPLETED
            DwellBundleStatus.SCHEDULED -> return next == DwellBundleStatus.COMPLETED || next == DwellBundleStatus.CANCELLED
            DwellBundleStatus.IN_PROGRESS -> return next == DwellBundleStatus.COMPLETED
            DwellBundleStatus.COMPLETED -> {
                when (next) {
                    DwellBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellBundleStatus) {
        if (!canMoveTo(next)) {
            error("dwell bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((sequence.toDouble() - threshold) * 100) / 100

    fun snapshot(): DwellBundle {
        return DwellBundle(
            code = code,
            label = label,
            revision = revision,
            approved = approved,
            threshold = threshold,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell bundle in status planned. */
fun newDwellBundle(code: String, label: String): DwellBundle {
    return DwellBundle(
        code = code,
        label = label,
        revision = 0,
        approved = false,
        threshold = 0.0,
        sequence = 0,
        status = DwellBundleStatus.PLANNED,
    )
}
