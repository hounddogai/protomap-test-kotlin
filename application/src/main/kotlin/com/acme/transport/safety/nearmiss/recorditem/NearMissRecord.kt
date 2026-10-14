package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss record records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss record is in its life cycle. */
enum class NearMissRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A near miss record as the near miss team keeps it. */
data class NearMissRecord(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var name: String,
    var ceiling: Double,
    var contactEmail: String,
    var currency: String,
    var status: NearMissRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissRecordStatus): Boolean {
        when (status) {
            NearMissRecordStatus.NEW -> {
                when (next) {
                    NearMissRecordStatus.QUEUED -> return true
                }
                return false
            }
            NearMissRecordStatus.QUEUED -> return next == NearMissRecordStatus.FAILED
            NearMissRecordStatus.RUNNING -> return next == NearMissRecordStatus.DONE
            NearMissRecordStatus.DONE -> return next == NearMissRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: NearMissRecordStatus) {
        if (!canMoveTo(next)) {
            error("near miss record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((durationMinutes.toDouble() - ceiling) * 100) / 100

    fun snapshot(): NearMissRecord {
        return NearMissRecord(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            name = name,
            ceiling = ceiling,
            contactEmail = contactEmail,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss record in status new. */
fun newNearMissRecord(code: String, title: String): NearMissRecord {
    return NearMissRecord(
        code = code,
        title = title,
        durationMinutes = 0,
        name = "",
        ceiling = 0.0,
        contactEmail = "",
        currency = "",
        status = NearMissRecordStatus.NEW,
    )
}
