package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.kit.text.normalize

/** Where a accessorial window is in its life cycle. */
enum class AccessorialWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a accessorial window and its current status. */
data class AccessorialWindow(
    var code: String,
    var title: String,
    var shiftCode: String,
    var source: String,
    var stopCount: Int,
    var status: AccessorialWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialWindowStatus): Boolean {
        when (status) {
            AccessorialWindowStatus.NEW -> {
                when (next) {
                    AccessorialWindowStatus.QUEUED -> return true
                }
                return false
            }
            AccessorialWindowStatus.QUEUED -> return next == AccessorialWindowStatus.RUNNING || next == AccessorialWindowStatus.DONE
            AccessorialWindowStatus.RUNNING -> {
                when (next) {
                    AccessorialWindowStatus.DONE -> return true
                }
                return false
            }
            AccessorialWindowStatus.DONE -> return next == AccessorialWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AccessorialWindowStatus) {
        if (!canMoveTo(next)) {
            error("accessorial window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): AccessorialWindow {
        return AccessorialWindow(
            code = code,
            title = title,
            shiftCode = shiftCode,
            source = source,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a accessorial window in status new. */
fun newAccessorialWindow(code: String, title: String): AccessorialWindow {
    return AccessorialWindow(
        code = code,
        title = title,
        shiftCode = "",
        source = "",
        stopCount = 0,
        status = AccessorialWindowStatus.NEW,
    )
}
