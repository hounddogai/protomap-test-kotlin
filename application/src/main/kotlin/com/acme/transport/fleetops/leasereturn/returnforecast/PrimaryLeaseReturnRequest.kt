package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary lease return request is in its life cycle. */
enum class PrimaryLeaseReturnRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary lease return request. Its code is unique within a tenant. */
data class PrimaryLeaseReturnRequest(
    var code: String,
    var title: String,
    var version: Int,
    var windowDays: Int,
    var timezone: String,
    var maximum: Double,
    var phone: String,
    var quantity: Int,
    var notes: String,
    var revision: Int,
    var priority: Int,
    var status: PrimaryLeaseReturnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLeaseReturnRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLeaseReturnRequestStatus): Boolean {
        when (status) {
            PrimaryLeaseReturnRequestStatus.DRAFT -> {
                when (next) {
                    PrimaryLeaseReturnRequestStatus.PENDING, PrimaryLeaseReturnRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryLeaseReturnRequestStatus.PENDING -> {
                when (next) {
                    PrimaryLeaseReturnRequestStatus.SUSPENDED, PrimaryLeaseReturnRequestStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryLeaseReturnRequestStatus.ACTIVE -> {
                when (next) {
                    PrimaryLeaseReturnRequestStatus.SUSPENDED, PrimaryLeaseReturnRequestStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryLeaseReturnRequestStatus.SUSPENDED -> {
                when (next) {
                    PrimaryLeaseReturnRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLeaseReturnRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary lease return request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"primary lease return request"} $code"

    fun weightedLoad(): Double = round((windowDays.toDouble() - maximum) * 100) / 100

    fun snapshot(): PrimaryLeaseReturnRequest {
        return PrimaryLeaseReturnRequest(
            code = code,
            title = title,
            version = version,
            windowDays = windowDays,
            timezone = timezone,
            maximum = maximum,
            phone = phone,
            quantity = quantity,
            notes = notes,
            revision = revision,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary lease return request in status draft. */
fun newPrimaryLeaseReturnRequest(code: String, title: String): PrimaryLeaseReturnRequest {
    return PrimaryLeaseReturnRequest(
        code = code,
        title = title,
        version = 0,
        windowDays = 0,
        timezone = "",
        maximum = 0.0,
        phone = "",
        quantity = 0,
        notes = "",
        revision = 0,
        priority = 0,
        status = PrimaryLeaseReturnRequestStatus.DRAFT,
    )
}
