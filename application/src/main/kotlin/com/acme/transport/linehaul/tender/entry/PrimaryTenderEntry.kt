package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary tender entry records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a primary tender entry is in its life cycle. */
enum class PrimaryTenderEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary tender entry, with what the planners need to act on it. */
data class PrimaryTenderEntry(
    var code: String,
    var title: String,
    var endsAt: Int,
    var ownerId: String,
    var minimum: Double,
    var weight: Double,
    var durationMinutes: Int,
    var checksum: String,
    var stopCount: Int,
    var visible: Boolean,
    var status: PrimaryTenderEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderEntryStatus): Boolean {
        when (status) {
            PrimaryTenderEntryStatus.NEW -> {
                when (next) {
                    PrimaryTenderEntryStatus.RUNNING, PrimaryTenderEntryStatus.FAILED -> return true
                }
                return false
            }
            PrimaryTenderEntryStatus.QUEUED -> {
                when (next) {
                    PrimaryTenderEntryStatus.RUNNING -> return true
                }
                return false
            }
            PrimaryTenderEntryStatus.RUNNING -> return next == PrimaryTenderEntryStatus.DONE
            PrimaryTenderEntryStatus.DONE -> {
                when (next) {
                    PrimaryTenderEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTenderEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary tender entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((weight - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTenderEntry {
        return PrimaryTenderEntry(
            code = code,
            title = title,
            endsAt = endsAt,
            ownerId = ownerId,
            minimum = minimum,
            weight = weight,
            durationMinutes = durationMinutes,
            checksum = checksum,
            stopCount = stopCount,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary tender entry in status new. */
fun newPrimaryTenderEntry(code: String, title: String): PrimaryTenderEntry {
    return PrimaryTenderEntry(
        code = code,
        title = title,
        endsAt = 0,
        ownerId = "",
        minimum = 0.0,
        weight = 0.0,
        durationMinutes = 0,
        checksum = "",
        stopCount = 0,
        visible = false,
        status = PrimaryTenderEntryStatus.NEW,
    )
}
