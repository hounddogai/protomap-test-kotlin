package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation window is in its life cycle. */
enum class ViolationWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A violation window as the violation team keeps it. */
data class ViolationWindow(
    var code: String,
    var name: String,
    var windowDays: Int,
    var title: String,
    var threshold: Double,
    var amount: Double,
    var status: ViolationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationWindowStatus): Boolean {
        when (status) {
            ViolationWindowStatus.IDLE -> {
                when (next) {
                    ViolationWindowStatus.READY -> return true
                }
                return false
            }
            ViolationWindowStatus.WARMING -> return next == ViolationWindowStatus.READY
            ViolationWindowStatus.READY -> return next == ViolationWindowStatus.DRAINING || next == ViolationWindowStatus.STOPPED
            ViolationWindowStatus.DRAINING -> {
                when (next) {
                    ViolationWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationWindowStatus) {
        if (!canMoveTo(next)) {
            error("violation window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"violation window"} $code"

    fun weightedVolume(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): ViolationWindow {
        return ViolationWindow(
            code = code,
            name = name,
            windowDays = windowDays,
            title = title,
            threshold = threshold,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation window in status idle. */
fun newViolationWindow(code: String, name: String): ViolationWindow {
    return ViolationWindow(
        code = code,
        name = name,
        windowDays = 0,
        title = "",
        threshold = 0.0,
        amount = 0.0,
        status = ViolationWindowStatus.IDLE,
    )
}
