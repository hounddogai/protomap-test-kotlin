package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending relay rollup is in its life cycle. */
enum class PendingRelayRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending relay rollup. Its code is unique within a tenant. */
data class PendingRelayRollup(
    var code: String,
    var title: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var expiresAt: Int,
    var batchSize: Int,
    var externalRef: String,
    var distanceKm: Double,
    var discount: Double,
    var status: PendingRelayRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayRollupStatus): Boolean {
        when (status) {
            PendingRelayRollupStatus.PLANNED -> {
                when (next) {
                    PendingRelayRollupStatus.IN_PROGRESS, PendingRelayRollupStatus.COMPLETED -> return true
                }
                return false
            }
            PendingRelayRollupStatus.SCHEDULED -> {
                when (next) {
                    PendingRelayRollupStatus.COMPLETED, PendingRelayRollupStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRelayRollupStatus.IN_PROGRESS -> return next == PendingRelayRollupStatus.CANCELLED
            PendingRelayRollupStatus.COMPLETED -> return next == PendingRelayRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingRelayRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending relay rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending relay rollup"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((batchSize.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PendingRelayRollup {
        return PendingRelayRollup(
            code = code,
            title = title,
            pallets = pallets,
            tags = tags,
            expiresAt = expiresAt,
            batchSize = batchSize,
            externalRef = externalRef,
            distanceKm = distanceKm,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending relay rollup in status planned. */
fun newPendingRelayRollup(code: String, title: String): PendingRelayRollup {
    return PendingRelayRollup(
        code = code,
        title = title,
        pallets = 0,
        tags = mutableListOf<String>(),
        expiresAt = 0,
        batchSize = 0,
        externalRef = "",
        distanceKm = 0.0,
        discount = 0.0,
        status = PendingRelayRollupStatus.PLANNED,
    )
}
