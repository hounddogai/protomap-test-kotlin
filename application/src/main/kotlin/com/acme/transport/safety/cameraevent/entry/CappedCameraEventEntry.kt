package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped camera event entry records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a capped camera event entry is in its life cycle. */
enum class CappedCameraEventEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capped camera event entry, with what the planners need to act on it. */
data class CappedCameraEventEntry(
    var code: String,
    var name: String,
    var source: String,
    var active: Boolean,
    var pinned: Boolean,
    var etaMinutes: Int,
    var trailerId: String,
    var minimum: Double,
    var phone: String,
    var batchSize: Int,
    var status: CappedCameraEventEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCameraEventEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCameraEventEntryStatus): Boolean {
        when (status) {
            CappedCameraEventEntryStatus.REQUESTED -> return next == CappedCameraEventEntryStatus.REVOKED || next == CappedCameraEventEntryStatus.EXPIRED
            CappedCameraEventEntryStatus.GRANTED -> {
                when (next) {
                    CappedCameraEventEntryStatus.REVOKED, CappedCameraEventEntryStatus.EXPIRED -> return true
                }
                return false
            }
            CappedCameraEventEntryStatus.REVOKED -> return next == CappedCameraEventEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedCameraEventEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped camera event entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((batchSize.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedCameraEventEntry {
        return CappedCameraEventEntry(
            code = code,
            name = name,
            source = source,
            active = active,
            pinned = pinned,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            minimum = minimum,
            phone = phone,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped camera event entry in status requested. */
fun newCappedCameraEventEntry(code: String, name: String): CappedCameraEventEntry {
    return CappedCameraEventEntry(
        code = code,
        name = name,
        source = "",
        active = false,
        pinned = false,
        etaMinutes = 0,
        trailerId = "",
        minimum = 0.0,
        phone = "",
        batchSize = 0,
        status = CappedCameraEventEntryStatus.REQUESTED,
    )
}
