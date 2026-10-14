package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched backhaul digest is in its life cycle. */
enum class BatchedBackhaulDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched backhaul digest. Its code is unique within a tenant. */
data class BatchedBackhaulDigest(
    var code: String,
    var title: String,
    var endsAt: Int,
    var vehicleId: String,
    var reviewer: String,
    var fuelLiters: Double,
    var weight: Double,
    var pinned: Boolean,
    var visible: Boolean,
    var status: BatchedBackhaulDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBackhaulDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBackhaulDigestStatus): Boolean {
        when (status) {
            BatchedBackhaulDigestStatus.REQUESTED -> return next == BatchedBackhaulDigestStatus.GRANTED || next == BatchedBackhaulDigestStatus.REVOKED
            BatchedBackhaulDigestStatus.GRANTED -> return next == BatchedBackhaulDigestStatus.REVOKED || next == BatchedBackhaulDigestStatus.EXPIRED
            BatchedBackhaulDigestStatus.REVOKED -> {
                when (next) {
                    BatchedBackhaulDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedBackhaulDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched backhaul digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((endsAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): BatchedBackhaulDigest {
        return BatchedBackhaulDigest(
            code = code,
            title = title,
            endsAt = endsAt,
            vehicleId = vehicleId,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            weight = weight,
            pinned = pinned,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched backhaul digest in status requested. */
fun newBatchedBackhaulDigest(code: String, title: String): BatchedBackhaulDigest {
    return BatchedBackhaulDigest(
        code = code,
        title = title,
        endsAt = 0,
        vehicleId = "",
        reviewer = "",
        fuelLiters = 0.0,
        weight = 0.0,
        pinned = false,
        visible = false,
        status = BatchedBackhaulDigestStatus.REQUESTED,
    )
}
