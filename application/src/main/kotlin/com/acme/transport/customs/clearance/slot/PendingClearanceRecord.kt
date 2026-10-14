package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending clearance record is in its life cycle. */
enum class PendingClearanceRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending clearance record, with what the planners need to act on it. */
data class PendingClearanceRecord(
    var code: String,
    var name: String,
    var startsAt: Int,
    var contactEmail: String,
    var score: Double,
    var createdAt: Int,
    var loadKg: Double,
    var description: String,
    var status: PendingClearanceRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingClearanceRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingClearanceRecordStatus): Boolean {
        when (status) {
            PendingClearanceRecordStatus.CREATED -> {
                when (next) {
                    PendingClearanceRecordStatus.PUBLISHED, PendingClearanceRecordStatus.RETIRED -> return true
                }
                return false
            }
            PendingClearanceRecordStatus.VALIDATED -> {
                when (next) {
                    PendingClearanceRecordStatus.PUBLISHED, PendingClearanceRecordStatus.RETIRED -> return true
                }
                return false
            }
            PendingClearanceRecordStatus.PUBLISHED -> {
                when (next) {
                    PendingClearanceRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingClearanceRecordStatus) {
        if (!canMoveTo(next)) {
            error("pending clearance record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round(score * createdAt.toDouble() * 100) / 100

    fun snapshot(): PendingClearanceRecord {
        return PendingClearanceRecord(
            code = code,
            name = name,
            startsAt = startsAt,
            contactEmail = contactEmail,
            score = score,
            createdAt = createdAt,
            loadKg = loadKg,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending clearance record in status created. */
fun newPendingClearanceRecord(code: String, name: String): PendingClearanceRecord {
    return PendingClearanceRecord(
        code = code,
        name = name,
        startsAt = 0,
        contactEmail = "",
        score = 0.0,
        createdAt = 0,
        loadKg = 0.0,
        description = "",
        status = PendingClearanceRecordStatus.CREATED,
    )
}
