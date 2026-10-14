package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary coaching record is in its life cycle. */
enum class PrimaryCoachingRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary coaching record as the coaching team keeps it. */
data class PrimaryCoachingRecord(
    var code: String,
    var title: String,
    var revision: Int,
    var summary: String,
    var loadKg: Double,
    var attributes: MutableMap<String, String>,
    var trailerId: String,
    var carrier: String,
    var retries: Int,
    var name: String,
    var status: PrimaryCoachingRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCoachingRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCoachingRecordStatus): Boolean {
        when (status) {
            PrimaryCoachingRecordStatus.IDLE -> {
                when (next) {
                    PrimaryCoachingRecordStatus.WARMING, PrimaryCoachingRecordStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryCoachingRecordStatus.WARMING -> return next == PrimaryCoachingRecordStatus.READY || next == PrimaryCoachingRecordStatus.STOPPED
            PrimaryCoachingRecordStatus.READY -> {
                when (next) {
                    PrimaryCoachingRecordStatus.DRAINING, PrimaryCoachingRecordStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryCoachingRecordStatus.DRAINING -> return next == PrimaryCoachingRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryCoachingRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary coaching record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"primary coaching record"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((retries.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCoachingRecord {
        return PrimaryCoachingRecord(
            code = code,
            title = title,
            revision = revision,
            summary = summary,
            loadKg = loadKg,
            attributes = attributes,
            trailerId = trailerId,
            carrier = carrier,
            retries = retries,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary coaching record in status idle. */
fun newPrimaryCoachingRecord(code: String, title: String): PrimaryCoachingRecord {
    return PrimaryCoachingRecord(
        code = code,
        title = title,
        revision = 0,
        summary = "",
        loadKg = 0.0,
        attributes = mutableMapOf<String, String>(),
        trailerId = "",
        carrier = "",
        retries = 0,
        name = "",
        status = PrimaryCoachingRecordStatus.IDLE,
    )
}
