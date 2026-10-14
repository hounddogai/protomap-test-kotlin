package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dwell allocation is in its life cycle. */
enum class ScheduledDwellAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled dwell allocation. Its code is unique within a tenant. */
data class ScheduledDwellAllocation(
    var code: String,
    var title: String,
    var limit: Int,
    var contactEmail: String,
    var etaMinutes: Int,
    var pallets: Int,
    var updatedAt: Int,
    var category: String,
    var loadKg: Double,
    var segment: String,
    var status: ScheduledDwellAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDwellAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDwellAllocationStatus): Boolean {
        when (status) {
            ScheduledDwellAllocationStatus.NEW -> {
                when (next) {
                    ScheduledDwellAllocationStatus.QUEUED, ScheduledDwellAllocationStatus.FAILED -> return true
                }
                return false
            }
            ScheduledDwellAllocationStatus.QUEUED -> return next == ScheduledDwellAllocationStatus.FAILED
            ScheduledDwellAllocationStatus.RUNNING -> {
                when (next) {
                    ScheduledDwellAllocationStatus.DONE, ScheduledDwellAllocationStatus.FAILED -> return true
                }
                return false
            }
            ScheduledDwellAllocationStatus.DONE -> return next == ScheduledDwellAllocationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledDwellAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dwell allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round(limit.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ScheduledDwellAllocation {
        return ScheduledDwellAllocation(
            code = code,
            title = title,
            limit = limit,
            contactEmail = contactEmail,
            etaMinutes = etaMinutes,
            pallets = pallets,
            updatedAt = updatedAt,
            category = category,
            loadKg = loadKg,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled dwell allocation in status new. */
fun newScheduledDwellAllocation(code: String, title: String): ScheduledDwellAllocation {
    return ScheduledDwellAllocation(
        code = code,
        title = title,
        limit = 0,
        contactEmail = "",
        etaMinutes = 0,
        pallets = 0,
        updatedAt = 0,
        category = "",
        loadKg = 0.0,
        segment = "",
        status = ScheduledDwellAllocationStatus.NEW,
    )
}
