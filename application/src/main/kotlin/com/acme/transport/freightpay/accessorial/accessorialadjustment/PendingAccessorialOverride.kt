package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending accessorial override is in its life cycle. */
enum class PendingAccessorialOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending accessorial override. Its code is unique within a tenant. */
data class PendingAccessorialOverride(
    var code: String,
    var title: String,
    var segment: String,
    var budget: Double,
    var pallets: Int,
    var archived: Boolean,
    var channel: String,
    var status: PendingAccessorialOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAccessorialOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAccessorialOverrideStatus): Boolean {
        when (status) {
            PendingAccessorialOverrideStatus.DRAFT -> return next == PendingAccessorialOverrideStatus.ACTIVE
            PendingAccessorialOverrideStatus.PENDING -> {
                when (next) {
                    PendingAccessorialOverrideStatus.SUSPENDED, PendingAccessorialOverrideStatus.CLOSED -> return true
                }
                return false
            }
            PendingAccessorialOverrideStatus.ACTIVE -> {
                when (next) {
                    PendingAccessorialOverrideStatus.SUSPENDED, PendingAccessorialOverrideStatus.CLOSED -> return true
                }
                return false
            }
            PendingAccessorialOverrideStatus.SUSPENDED -> {
                when (next) {
                    PendingAccessorialOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAccessorialOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending accessorial override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((pallets.toDouble() - budget) * 100) / 100

    fun snapshot(): PendingAccessorialOverride {
        return PendingAccessorialOverride(
            code = code,
            title = title,
            segment = segment,
            budget = budget,
            pallets = pallets,
            archived = archived,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending accessorial override in status draft. */
fun newPendingAccessorialOverride(code: String, title: String): PendingAccessorialOverride {
    return PendingAccessorialOverride(
        code = code,
        title = title,
        segment = "",
        budget = 0.0,
        pallets = 0,
        archived = false,
        channel = "",
        status = PendingAccessorialOverrideStatus.DRAFT,
    )
}
