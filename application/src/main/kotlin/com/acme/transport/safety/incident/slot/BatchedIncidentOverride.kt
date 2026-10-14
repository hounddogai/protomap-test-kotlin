package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched incident override is in its life cycle. */
enum class BatchedIncidentOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched incident override and its current status. */
data class BatchedIncidentOverride(
    var code: String,
    var label: String,
    var dock: String,
    var pinned: Boolean,
    var currency: String,
    var checksum: String,
    var routeCode: String,
    var expiresAt: Int,
    var timezone: String,
    var minimum: Double,
    var pallets: Int,
    var status: BatchedIncidentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIncidentOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIncidentOverrideStatus): Boolean {
        when (status) {
            BatchedIncidentOverrideStatus.OPEN -> {
                when (next) {
                    BatchedIncidentOverrideStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedIncidentOverrideStatus.IN_REVIEW -> return next == BatchedIncidentOverrideStatus.RESOLVED || next == BatchedIncidentOverrideStatus.REOPENED
            BatchedIncidentOverrideStatus.RESOLVED -> return next == BatchedIncidentOverrideStatus.REOPENED || next == BatchedIncidentOverrideStatus.ARCHIVED
            BatchedIncidentOverrideStatus.REOPENED -> {
                when (next) {
                    BatchedIncidentOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedIncidentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("batched incident override $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(minimum * pallets.toDouble() * 100) / 100

    fun snapshot(): BatchedIncidentOverride {
        return BatchedIncidentOverride(
            code = code,
            label = label,
            dock = dock,
            pinned = pinned,
            currency = currency,
            checksum = checksum,
            routeCode = routeCode,
            expiresAt = expiresAt,
            timezone = timezone,
            minimum = minimum,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched incident override in status open. */
fun newBatchedIncidentOverride(code: String, label: String): BatchedIncidentOverride {
    return BatchedIncidentOverride(
        code = code,
        label = label,
        dock = "",
        pinned = false,
        currency = "",
        checksum = "",
        routeCode = "",
        expiresAt = 0,
        timezone = "",
        minimum = 0.0,
        pallets = 0,
        status = BatchedIncidentOverrideStatus.OPEN,
    )
}
