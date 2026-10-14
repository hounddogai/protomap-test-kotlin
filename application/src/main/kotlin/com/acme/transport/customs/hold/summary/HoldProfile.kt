package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize

/** Where a hold profile is in its life cycle. */
enum class HoldProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a hold profile and its current status. */
data class HoldProfile(
    var code: String,
    var label: String,
    var rate: Double,
    var visible: Boolean,
    var shiftCode: String,
    var status: HoldProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldProfileStatus): Boolean {
        when (status) {
            HoldProfileStatus.IDLE -> return next == HoldProfileStatus.WARMING || next == HoldProfileStatus.STOPPED
            HoldProfileStatus.WARMING -> {
                when (next) {
                    HoldProfileStatus.DRAINING, HoldProfileStatus.STOPPED -> return true
                }
                return false
            }
            HoldProfileStatus.READY -> {
                when (next) {
                    HoldProfileStatus.DRAINING, HoldProfileStatus.STOPPED -> return true
                }
                return false
            }
            HoldProfileStatus.DRAINING -> {
                when (next) {
                    HoldProfileStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldProfileStatus) {
        if (!canMoveTo(next)) {
            error("hold profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"hold profile"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): HoldProfile {
        return HoldProfile(
            code = code,
            label = label,
            rate = rate,
            visible = visible,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a hold profile in status idle. */
fun newHoldProfile(code: String, label: String): HoldProfile {
    return HoldProfile(
        code = code,
        label = label,
        rate = 0.0,
        visible = false,
        shiftCode = "",
        status = HoldProfileStatus.IDLE,
    )
}
