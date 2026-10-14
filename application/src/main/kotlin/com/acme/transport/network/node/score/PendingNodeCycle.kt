package com.acme.transport.network.node.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending node cycle is in its life cycle. */
enum class PendingNodeCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending node cycle as the node team keeps it. */
data class PendingNodeCycle(
    var code: String,
    var title: String,
    var createdAt: Int,
    var ceiling: Double,
    var approved: Boolean,
    var discount: Double,
    var contactEmail: String,
    var summary: String,
    var revision: Int,
    var margin: Double,
    var status: PendingNodeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeCycleStatus): Boolean {
        when (status) {
            PendingNodeCycleStatus.CREATED -> return next == PendingNodeCycleStatus.VALIDATED
            PendingNodeCycleStatus.VALIDATED -> return next == PendingNodeCycleStatus.PUBLISHED || next == PendingNodeCycleStatus.RETIRED
            PendingNodeCycleStatus.PUBLISHED -> {
                when (next) {
                    PendingNodeCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNodeCycleStatus) {
        if (!canMoveTo(next)) {
            error("pending node cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"pending node cycle"} $code"

    fun weightedLoad(): Double = round((createdAt.toDouble() + margin) * 100) / 100

    fun snapshot(): PendingNodeCycle {
        return PendingNodeCycle(
            code = code,
            title = title,
            createdAt = createdAt,
            ceiling = ceiling,
            approved = approved,
            discount = discount,
            contactEmail = contactEmail,
            summary = summary,
            revision = revision,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending node cycle in status created. */
fun newPendingNodeCycle(code: String, title: String): PendingNodeCycle {
    return PendingNodeCycle(
        code = code,
        title = title,
        createdAt = 0,
        ceiling = 0.0,
        approved = false,
        discount = 0.0,
        contactEmail = "",
        summary = "",
        revision = 0,
        margin = 0.0,
        status = PendingNodeCycleStatus.CREATED,
    )
}
