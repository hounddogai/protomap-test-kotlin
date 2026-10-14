package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.normalize

/** Where a lenient transit time snapshot is in its life cycle. */
enum class LenientTransitTimeSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient transit time snapshot and its current status. */
data class LenientTransitTimeSnapshot(
    var code: String,
    var name: String,
    var category: String,
    var locale: String,
    var margin: Double,
    var status: LenientTransitTimeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTransitTimeSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTransitTimeSnapshotStatus): Boolean {
        when (status) {
            LenientTransitTimeSnapshotStatus.REQUESTED -> {
                when (next) {
                    LenientTransitTimeSnapshotStatus.GRANTED, LenientTransitTimeSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            LenientTransitTimeSnapshotStatus.GRANTED -> return next == LenientTransitTimeSnapshotStatus.REVOKED || next == LenientTransitTimeSnapshotStatus.EXPIRED
            LenientTransitTimeSnapshotStatus.REVOKED -> {
                when (next) {
                    LenientTransitTimeSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTransitTimeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lenient transit time snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient transit time snapshot"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): LenientTransitTimeSnapshot {
        return LenientTransitTimeSnapshot(
            code = code,
            name = name,
            category = category,
            locale = locale,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient transit time snapshot in status requested. */
fun newLenientTransitTimeSnapshot(code: String, name: String): LenientTransitTimeSnapshot {
    return LenientTransitTimeSnapshot(
        code = code,
        name = name,
        category = "",
        locale = "",
        margin = 0.0,
        status = LenientTransitTimeSnapshotStatus.REQUESTED,
    )
}
