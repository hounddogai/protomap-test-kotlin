package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.text.normalize

/** Where a cross dock baseline is in its life cycle. */
enum class CrossDockBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock baseline as the cross dock team keeps it. */
data class CrossDockBaseline(
    var code: String,
    var name: String,
    var visible: Boolean,
    var description: String,
    var endsAt: Int,
    var status: CrossDockBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockBaselineStatus): Boolean {
        when (status) {
            CrossDockBaselineStatus.IDLE -> return next == CrossDockBaselineStatus.DRAINING || next == CrossDockBaselineStatus.STOPPED
            CrossDockBaselineStatus.WARMING -> {
                when (next) {
                    CrossDockBaselineStatus.DRAINING, CrossDockBaselineStatus.STOPPED -> return true
                }
                return false
            }
            CrossDockBaselineStatus.READY -> {
                when (next) {
                    CrossDockBaselineStatus.DRAINING, CrossDockBaselineStatus.STOPPED -> return true
                }
                return false
            }
            CrossDockBaselineStatus.DRAINING -> {
                when (next) {
                    CrossDockBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockBaselineStatus) {
        if (!canMoveTo(next)) {
            error("cross dock baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"cross dock baseline"} $code"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): CrossDockBaseline {
        return CrossDockBaseline(
            code = code,
            name = name,
            visible = visible,
            description = description,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cross dock baseline in status idle. */
fun newCrossDockBaseline(code: String, name: String): CrossDockBaseline {
    return CrossDockBaseline(
        code = code,
        name = name,
        visible = false,
        description = "",
        endsAt = 0,
        status = CrossDockBaselineStatus.IDLE,
    )
}
