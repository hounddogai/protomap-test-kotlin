package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop estimate is in its life cycle. */
enum class DropEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A drop estimate as the drop team keeps it. */
data class DropEstimate(
    var code: String,
    var label: String,
    var createdAt: Int,
    var threshold: Double,
    var updatedAt: Int,
    var status: DropEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropEstimateStatus): Boolean {
        when (status) {
            DropEstimateStatus.REQUESTED -> {
                when (next) {
                    DropEstimateStatus.GRANTED, DropEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            DropEstimateStatus.GRANTED -> return next == DropEstimateStatus.REVOKED || next == DropEstimateStatus.EXPIRED
            DropEstimateStatus.REVOKED -> {
                when (next) {
                    DropEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropEstimateStatus) {
        if (!canMoveTo(next)) {
            error("drop estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round(createdAt.toDouble() * threshold * 100) / 100

    fun snapshot(): DropEstimate {
        return DropEstimate(
            code = code,
            label = label,
            createdAt = createdAt,
            threshold = threshold,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop estimate in status requested. */
fun newDropEstimate(code: String, label: String): DropEstimate {
    return DropEstimate(
        code = code,
        label = label,
        createdAt = 0,
        threshold = 0.0,
        updatedAt = 0,
        status = DropEstimateStatus.REQUESTED,
    )
}
