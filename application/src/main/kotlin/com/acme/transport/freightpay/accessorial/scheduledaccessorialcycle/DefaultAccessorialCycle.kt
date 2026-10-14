package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default accessorial cycle records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a default accessorial cycle is in its life cycle. */
enum class DefaultAccessorialCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default accessorial cycle. Its code is unique within a tenant. */
data class DefaultAccessorialCycle(
    var code: String,
    var name: String,
    var version: Int,
    var routeCode: String,
    var tags: MutableList<String>,
    var etaMinutes: Int,
    var fuelLiters: Double,
    var status: DefaultAccessorialCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAccessorialCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAccessorialCycleStatus): Boolean {
        when (status) {
            DefaultAccessorialCycleStatus.NEW -> return next == DefaultAccessorialCycleStatus.RUNNING
            DefaultAccessorialCycleStatus.QUEUED -> return next == DefaultAccessorialCycleStatus.FAILED
            DefaultAccessorialCycleStatus.RUNNING -> return next == DefaultAccessorialCycleStatus.FAILED
            DefaultAccessorialCycleStatus.DONE -> {
                when (next) {
                    DefaultAccessorialCycleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultAccessorialCycleStatus) {
        if (!canMoveTo(next)) {
            error("default accessorial cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"default accessorial cycle"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((version.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultAccessorialCycle {
        return DefaultAccessorialCycle(
            code = code,
            name = name,
            version = version,
            routeCode = routeCode,
            tags = tags,
            etaMinutes = etaMinutes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default accessorial cycle in status new. */
fun newDefaultAccessorialCycle(code: String, name: String): DefaultAccessorialCycle {
    return DefaultAccessorialCycle(
        code = code,
        name = name,
        version = 0,
        routeCode = "",
        tags = mutableListOf<String>(),
        etaMinutes = 0,
        fuelLiters = 0.0,
        status = DefaultAccessorialCycleStatus.NEW,
    )
}
