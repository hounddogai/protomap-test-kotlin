package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return request is in its life cycle. */
enum class LeaseReturnRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lease return request. Its code is unique within a tenant. */
data class LeaseReturnRequest(
    var code: String,
    var title: String,
    var maximum: Double,
    var createdAt: Int,
    var category: String,
    var region: String,
    var channel: String,
    var revision: Int,
    var windowDays: Int,
    var status: LeaseReturnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnRequestStatus): Boolean {
        when (status) {
            LeaseReturnRequestStatus.REQUESTED -> return next == LeaseReturnRequestStatus.REVOKED
            LeaseReturnRequestStatus.GRANTED -> {
                when (next) {
                    LeaseReturnRequestStatus.REVOKED -> return true
                }
                return false
            }
            LeaseReturnRequestStatus.REVOKED -> {
                when (next) {
                    LeaseReturnRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnRequestStatus) {
        if (!canMoveTo(next)) {
            error("lease return request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((maximum + createdAt.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnRequest {
        return LeaseReturnRequest(
            code = code,
            title = title,
            maximum = maximum,
            createdAt = createdAt,
            category = category,
            region = region,
            channel = channel,
            revision = revision,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lease return request in status requested. */
fun newLeaseReturnRequest(code: String, title: String): LeaseReturnRequest {
    return LeaseReturnRequest(
        code = code,
        title = title,
        maximum = 0.0,
        createdAt = 0,
        category = "",
        region = "",
        channel = "",
        revision = 0,
        windowDays = 0,
        status = LeaseReturnRequestStatus.REQUESTED,
    )
}
