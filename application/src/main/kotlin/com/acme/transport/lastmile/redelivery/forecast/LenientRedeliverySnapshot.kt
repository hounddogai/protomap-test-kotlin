package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient redelivery snapshot is in its life cycle. */
enum class LenientRedeliverySnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient redelivery snapshot, with what the planners need to act on it. */
data class LenientRedeliverySnapshot(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var loadKg: Double,
    var version: Int,
    var reviewer: String,
    var capacity: Int,
    var status: LenientRedeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRedeliverySnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRedeliverySnapshotStatus): Boolean {
        when (status) {
            LenientRedeliverySnapshotStatus.REQUESTED -> return next == LenientRedeliverySnapshotStatus.REVOKED
            LenientRedeliverySnapshotStatus.GRANTED -> {
                when (next) {
                    LenientRedeliverySnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            LenientRedeliverySnapshotStatus.REVOKED -> return next == LenientRedeliverySnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientRedeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lenient redelivery snapshot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round(capacity.toDouble() * loadKg * 100) / 100

    fun snapshot(): LenientRedeliverySnapshot {
        return LenientRedeliverySnapshot(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            version = version,
            reviewer = reviewer,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient redelivery snapshot in status requested. */
fun newLenientRedeliverySnapshot(code: String, label: String): LenientRedeliverySnapshot {
    return LenientRedeliverySnapshot(
        code = code,
        label = label,
        etaMinutes = 0,
        loadKg = 0.0,
        version = 0,
        reviewer = "",
        capacity = 0,
        status = LenientRedeliverySnapshotStatus.REQUESTED,
    )
}
