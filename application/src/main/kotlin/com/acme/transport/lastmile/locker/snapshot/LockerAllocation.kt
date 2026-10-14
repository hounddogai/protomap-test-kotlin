package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker allocation is in its life cycle. */
enum class LockerAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A locker allocation. Its code is unique within a tenant. */
data class LockerAllocation(
    var code: String,
    var name: String,
    var dock: String,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var timezone: String,
    var odometer: Int,
    var weight: Double,
    var priority: Int,
    var routeCode: String,
    var score: Double,
    var status: LockerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerAllocationStatus): Boolean {
        when (status) {
            LockerAllocationStatus.NEW -> return next == LockerAllocationStatus.RUNNING
            LockerAllocationStatus.QUEUED -> {
                when (next) {
                    LockerAllocationStatus.DONE -> return true
                }
                return false
            }
            LockerAllocationStatus.RUNNING -> return next == LockerAllocationStatus.DONE || next == LockerAllocationStatus.FAILED
            LockerAllocationStatus.DONE -> {
                when (next) {
                    LockerAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("locker allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((createdAt.toDouble() - weight) * 100) / 100

    fun snapshot(): LockerAllocation {
        return LockerAllocation(
            code = code,
            name = name,
            dock = dock,
            createdAt = createdAt,
            attributes = attributes,
            timezone = timezone,
            odometer = odometer,
            weight = weight,
            priority = priority,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker allocation in status new. */
fun newLockerAllocation(code: String, name: String): LockerAllocation {
    return LockerAllocation(
        code = code,
        name = name,
        dock = "",
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        timezone = "",
        odometer = 0,
        weight = 0.0,
        priority = 0,
        routeCode = "",
        score = 0.0,
        status = LockerAllocationStatus.NEW,
    )
}
