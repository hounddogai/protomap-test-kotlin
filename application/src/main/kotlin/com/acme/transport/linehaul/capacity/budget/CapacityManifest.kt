package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize

/** Where a capacity manifest is in its life cycle. */
enum class CapacityManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capacity manifest and its current status. */
data class CapacityManifest(
    var code: String,
    var title: String,
    var channel: String,
    var pinned: Boolean,
    var country: String,
    var amount: Double,
    var status: CapacityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityManifestStatus): Boolean {
        when (status) {
            CapacityManifestStatus.PLANNED -> {
                when (next) {
                    CapacityManifestStatus.COMPLETED -> return true
                }
                return false
            }
            CapacityManifestStatus.SCHEDULED -> return next == CapacityManifestStatus.IN_PROGRESS || next == CapacityManifestStatus.COMPLETED
            CapacityManifestStatus.IN_PROGRESS -> return next == CapacityManifestStatus.COMPLETED || next == CapacityManifestStatus.CANCELLED
            CapacityManifestStatus.COMPLETED -> {
                when (next) {
                    CapacityManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityManifestStatus) {
        if (!canMoveTo(next)) {
            error("capacity manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): CapacityManifest {
        return CapacityManifest(
            code = code,
            title = title,
            channel = channel,
            pinned = pinned,
            country = country,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capacity manifest in status planned. */
fun newCapacityManifest(code: String, title: String): CapacityManifest {
    return CapacityManifest(
        code = code,
        title = title,
        channel = "",
        pinned = false,
        country = "",
        amount = 0.0,
        status = CapacityManifestStatus.PLANNED,
    )
}
