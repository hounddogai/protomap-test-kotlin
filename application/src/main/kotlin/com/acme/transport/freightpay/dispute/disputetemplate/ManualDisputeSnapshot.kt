package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dispute snapshot is in its life cycle. */
enum class ManualDisputeSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual dispute snapshot. Its code is unique within a tenant. */
data class ManualDisputeSnapshot(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var unit: String,
    var phone: String,
    var endsAt: Int,
    var status: ManualDisputeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDisputeSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDisputeSnapshotStatus): Boolean {
        when (status) {
            ManualDisputeSnapshotStatus.PROPOSED -> {
                when (next) {
                    ManualDisputeSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualDisputeSnapshotStatus.APPROVED -> {
                when (next) {
                    ManualDisputeSnapshotStatus.REJECTED, ManualDisputeSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualDisputeSnapshotStatus.REJECTED -> return next == ManualDisputeSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualDisputeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("manual dispute snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual dispute snapshot"} $code"

    fun weightedVolume(): Double = round(durationMinutes.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): ManualDisputeSnapshot {
        return ManualDisputeSnapshot(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            unit = unit,
            phone = phone,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual dispute snapshot in status proposed. */
fun newManualDisputeSnapshot(code: String, label: String): ManualDisputeSnapshot {
    return ManualDisputeSnapshot(
        code = code,
        label = label,
        durationMinutes = 0,
        unit = "",
        phone = "",
        endsAt = 0,
        status = ManualDisputeSnapshotStatus.PROPOSED,
    )
}
