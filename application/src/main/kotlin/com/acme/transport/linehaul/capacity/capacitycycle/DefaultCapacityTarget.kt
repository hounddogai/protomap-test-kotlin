package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.normalize

/** Where a default capacity target is in its life cycle. */
enum class DefaultCapacityTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default capacity target. Its code is unique within a tenant. */
data class DefaultCapacityTarget(
    var code: String,
    var title: String,
    var source: String,
    var stopCount: Int,
    var routeCode: String,
    var status: DefaultCapacityTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCapacityTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCapacityTargetStatus): Boolean {
        when (status) {
            DefaultCapacityTargetStatus.IDLE -> return next == DefaultCapacityTargetStatus.READY
            DefaultCapacityTargetStatus.WARMING -> {
                when (next) {
                    DefaultCapacityTargetStatus.READY, DefaultCapacityTargetStatus.STOPPED -> return true
                }
                return false
            }
            DefaultCapacityTargetStatus.READY -> {
                when (next) {
                    DefaultCapacityTargetStatus.DRAINING, DefaultCapacityTargetStatus.STOPPED -> return true
                }
                return false
            }
            DefaultCapacityTargetStatus.DRAINING -> {
                when (next) {
                    DefaultCapacityTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCapacityTargetStatus) {
        if (!canMoveTo(next)) {
            error("default capacity target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): DefaultCapacityTarget {
        return DefaultCapacityTarget(
            code = code,
            title = title,
            source = source,
            stopCount = stopCount,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default capacity target in status idle. */
fun newDefaultCapacityTarget(code: String, title: String): DefaultCapacityTarget {
    return DefaultCapacityTarget(
        code = code,
        title = title,
        source = "",
        stopCount = 0,
        routeCode = "",
        status = DefaultCapacityTargetStatus.IDLE,
    )
}
