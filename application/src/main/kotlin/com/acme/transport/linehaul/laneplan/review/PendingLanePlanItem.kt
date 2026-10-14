package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending lane plan item is in its life cycle. */
enum class PendingLanePlanItemStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending lane plan item. Its code is unique within a tenant. */
data class PendingLanePlanItem(
    var code: String,
    var name: String,
    var ownerId: String,
    var ceiling: Double,
    var summary: String,
    var contactEmail: String,
    var endsAt: Int,
    var title: String,
    var reviewer: String,
    var status: PendingLanePlanItemStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLanePlanItemStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLanePlanItemStatus): Boolean {
        when (status) {
            PendingLanePlanItemStatus.IDLE -> {
                when (next) {
                    PendingLanePlanItemStatus.READY -> return true
                }
                return false
            }
            PendingLanePlanItemStatus.WARMING -> return next == PendingLanePlanItemStatus.READY
            PendingLanePlanItemStatus.READY -> {
                when (next) {
                    PendingLanePlanItemStatus.DRAINING -> return true
                }
                return false
            }
            PendingLanePlanItemStatus.DRAINING -> return next == PendingLanePlanItemStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingLanePlanItemStatus) {
        if (!canMoveTo(next)) {
            error("pending lane plan item $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round(endsAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): PendingLanePlanItem {
        return PendingLanePlanItem(
            code = code,
            name = name,
            ownerId = ownerId,
            ceiling = ceiling,
            summary = summary,
            contactEmail = contactEmail,
            endsAt = endsAt,
            title = title,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending lane plan item in status idle. */
fun newPendingLanePlanItem(code: String, name: String): PendingLanePlanItem {
    return PendingLanePlanItem(
        code = code,
        name = name,
        ownerId = "",
        ceiling = 0.0,
        summary = "",
        contactEmail = "",
        endsAt = 0,
        title = "",
        reviewer = "",
        status = PendingLanePlanItemStatus.IDLE,
    )
}
