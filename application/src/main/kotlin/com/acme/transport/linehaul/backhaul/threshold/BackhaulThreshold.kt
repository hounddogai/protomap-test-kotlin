package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.text.normalize

// The backhaul threshold records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul threshold is in its life cycle. */
enum class BackhaulThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a backhaul threshold and its current status. */
data class BackhaulThreshold(
    var code: String,
    var label: String,
    var name: String,
    var shiftCode: String,
    var graceDays: Int,
    var status: BackhaulThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulThresholdStatus): Boolean {
        when (status) {
            BackhaulThresholdStatus.REQUESTED -> return next == BackhaulThresholdStatus.GRANTED || next == BackhaulThresholdStatus.EXPIRED
            BackhaulThresholdStatus.GRANTED -> return next == BackhaulThresholdStatus.REVOKED
            BackhaulThresholdStatus.REVOKED -> {
                when (next) {
                    BackhaulThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulThresholdStatus) {
        if (!canMoveTo(next)) {
            error("backhaul threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): BackhaulThreshold {
        return BackhaulThreshold(
            code = code,
            label = label,
            name = name,
            shiftCode = shiftCode,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul threshold in status requested. */
fun newBackhaulThreshold(code: String, label: String): BackhaulThreshold {
    return BackhaulThreshold(
        code = code,
        label = label,
        name = "",
        shiftCode = "",
        graceDays = 0,
        status = BackhaulThresholdStatus.REQUESTED,
    )
}
