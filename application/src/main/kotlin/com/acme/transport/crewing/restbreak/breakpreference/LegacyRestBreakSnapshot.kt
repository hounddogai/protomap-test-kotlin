package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy rest break snapshot is in its life cycle. */
enum class LegacyRestBreakSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy rest break snapshot as the rest break team keeps it. */
data class LegacyRestBreakSnapshot(
    var code: String,
    var title: String,
    var phone: String,
    var capacity: Int,
    var retries: Int,
    var externalRef: String,
    var minimum: Double,
    var cost: Double,
    var checksum: String,
    var ratio: Double,
    var amount: Double,
    var status: LegacyRestBreakSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRestBreakSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRestBreakSnapshotStatus): Boolean {
        when (status) {
            LegacyRestBreakSnapshotStatus.PROPOSED -> {
                when (next) {
                    LegacyRestBreakSnapshotStatus.APPROVED, LegacyRestBreakSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            LegacyRestBreakSnapshotStatus.APPROVED -> {
                when (next) {
                    LegacyRestBreakSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            LegacyRestBreakSnapshotStatus.REJECTED -> {
                when (next) {
                    LegacyRestBreakSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRestBreakSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy rest break snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((retries.toDouble() + amount) * 100) / 100

    fun snapshot(): LegacyRestBreakSnapshot {
        return LegacyRestBreakSnapshot(
            code = code,
            title = title,
            phone = phone,
            capacity = capacity,
            retries = retries,
            externalRef = externalRef,
            minimum = minimum,
            cost = cost,
            checksum = checksum,
            ratio = ratio,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy rest break snapshot in status proposed. */
fun newLegacyRestBreakSnapshot(code: String, title: String): LegacyRestBreakSnapshot {
    return LegacyRestBreakSnapshot(
        code = code,
        title = title,
        phone = "",
        capacity = 0,
        retries = 0,
        externalRef = "",
        minimum = 0.0,
        cost = 0.0,
        checksum = "",
        ratio = 0.0,
        amount = 0.0,
        status = LegacyRestBreakSnapshotStatus.PROPOSED,
    )
}
