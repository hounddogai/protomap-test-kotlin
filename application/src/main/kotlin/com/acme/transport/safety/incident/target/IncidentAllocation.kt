package com.acme.transport.safety.incident.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident allocation is in its life cycle. */
enum class IncidentAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A incident allocation as the incident team keeps it. */
data class IncidentAllocation(
    var code: String,
    var name: String,
    var contactEmail: String,
    var title: String,
    var stopCount: Int,
    var durationMinutes: Int,
    var status: IncidentAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentAllocationStatus): Boolean {
        when (status) {
            IncidentAllocationStatus.NEW -> {
                when (next) {
                    IncidentAllocationStatus.RUNNING, IncidentAllocationStatus.FAILED -> return true
                }
                return false
            }
            IncidentAllocationStatus.QUEUED -> return next == IncidentAllocationStatus.RUNNING || next == IncidentAllocationStatus.DONE
            IncidentAllocationStatus.RUNNING -> return next == IncidentAllocationStatus.DONE || next == IncidentAllocationStatus.FAILED
            IncidentAllocationStatus.DONE -> {
                when (next) {
                    IncidentAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentAllocationStatus) {
        if (!canMoveTo(next)) {
            error("incident allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round(durationMinutes.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): IncidentAllocation {
        return IncidentAllocation(
            code = code,
            name = name,
            contactEmail = contactEmail,
            title = title,
            stopCount = stopCount,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a incident allocation in status new. */
fun newIncidentAllocation(code: String, name: String): IncidentAllocation {
    return IncidentAllocation(
        code = code,
        name = name,
        contactEmail = "",
        title = "",
        stopCount = 0,
        durationMinutes = 0,
        status = IncidentAllocationStatus.NEW,
    )
}
