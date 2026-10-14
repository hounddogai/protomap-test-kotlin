package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual drop record is in its life cycle. */
enum class ManualDropRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual drop record as the drop team keeps it. */
data class ManualDropRecord(
    var code: String,
    var label: String,
    var createdAt: Int,
    var budget: Double,
    var ownerId: String,
    var region: String,
    var status: ManualDropRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDropRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDropRecordStatus): Boolean {
        when (status) {
            ManualDropRecordStatus.PROPOSED -> {
                when (next) {
                    ManualDropRecordStatus.REJECTED, ManualDropRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualDropRecordStatus.APPROVED -> {
                when (next) {
                    ManualDropRecordStatus.REJECTED -> return true
                }
                return false
            }
            ManualDropRecordStatus.REJECTED -> return next == ManualDropRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualDropRecordStatus) {
        if (!canMoveTo(next)) {
            error("manual drop record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((createdAt.toDouble() + budget) * 100) / 100

    fun snapshot(): ManualDropRecord {
        return ManualDropRecord(
            code = code,
            label = label,
            createdAt = createdAt,
            budget = budget,
            ownerId = ownerId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual drop record in status proposed. */
fun newManualDropRecord(code: String, label: String): ManualDropRecord {
    return ManualDropRecord(
        code = code,
        label = label,
        createdAt = 0,
        budget = 0.0,
        ownerId = "",
        region = "",
        status = ManualDropRecordStatus.PROPOSED,
    )
}
