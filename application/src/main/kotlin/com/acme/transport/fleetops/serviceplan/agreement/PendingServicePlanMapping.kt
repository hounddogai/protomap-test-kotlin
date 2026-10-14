package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.text.normalize

/** Where a pending service plan mapping is in its life cycle. */
enum class PendingServicePlanMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending service plan mapping as the service plan team keeps it. */
data class PendingServicePlanMapping(
    var code: String,
    var label: String,
    var ownerId: String,
    var description: String,
    var segment: String,
    var dock: String,
    var title: String,
    var quantity: Int,
    var status: PendingServicePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServicePlanMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServicePlanMappingStatus): Boolean {
        when (status) {
            PendingServicePlanMappingStatus.NEW -> return next == PendingServicePlanMappingStatus.RUNNING || next == PendingServicePlanMappingStatus.DONE
            PendingServicePlanMappingStatus.QUEUED -> return next == PendingServicePlanMappingStatus.FAILED
            PendingServicePlanMappingStatus.RUNNING -> return next == PendingServicePlanMappingStatus.DONE || next == PendingServicePlanMappingStatus.FAILED
            PendingServicePlanMappingStatus.DONE -> return next == PendingServicePlanMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingServicePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("pending service plan mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending service plan mapping"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PendingServicePlanMapping {
        return PendingServicePlanMapping(
            code = code,
            label = label,
            ownerId = ownerId,
            description = description,
            segment = segment,
            dock = dock,
            title = title,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending service plan mapping in status new. */
fun newPendingServicePlanMapping(code: String, label: String): PendingServicePlanMapping {
    return PendingServicePlanMapping(
        code = code,
        label = label,
        ownerId = "",
        description = "",
        segment = "",
        dock = "",
        title = "",
        quantity = 0,
        status = PendingServicePlanMappingStatus.NEW,
    )
}
