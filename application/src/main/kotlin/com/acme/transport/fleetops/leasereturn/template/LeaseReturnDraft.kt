package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return draft is in its life cycle. */
enum class LeaseReturnDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lease return draft and its current status. */
data class LeaseReturnDraft(
    var code: String,
    var name: String,
    var maximum: Double,
    var version: Int,
    var updatedAt: Int,
    var limit: Int,
    var status: LeaseReturnDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnDraftStatus): Boolean {
        when (status) {
            LeaseReturnDraftStatus.CREATED -> {
                when (next) {
                    LeaseReturnDraftStatus.VALIDATED, LeaseReturnDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            LeaseReturnDraftStatus.VALIDATED -> return next == LeaseReturnDraftStatus.PUBLISHED || next == LeaseReturnDraftStatus.RETIRED
            LeaseReturnDraftStatus.PUBLISHED -> return next == LeaseReturnDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LeaseReturnDraftStatus) {
        if (!canMoveTo(next)) {
            error("lease return draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round(maximum * limit.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnDraft {
        return LeaseReturnDraft(
            code = code,
            name = name,
            maximum = maximum,
            version = version,
            updatedAt = updatedAt,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lease return draft in status created. */
fun newLeaseReturnDraft(code: String, name: String): LeaseReturnDraft {
    return LeaseReturnDraft(
        code = code,
        name = name,
        maximum = 0.0,
        version = 0,
        updatedAt = 0,
        limit = 0,
        status = LeaseReturnDraftStatus.CREATED,
    )
}
