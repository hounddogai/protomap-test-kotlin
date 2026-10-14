package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped near miss record is in its life cycle. */
enum class CappedNearMissRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capped near miss record, with what the planners need to act on it. */
data class CappedNearMissRecord(
    var code: String,
    var title: String,
    var startsAt: Int,
    var ceiling: Double,
    var category: String,
    var attempts: Int,
    var pallets: Int,
    var tags: MutableList<String>,
    var phone: String,
    var dock: String,
    var status: CappedNearMissRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNearMissRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNearMissRecordStatus): Boolean {
        when (status) {
            CappedNearMissRecordStatus.NEW -> {
                when (next) {
                    CappedNearMissRecordStatus.QUEUED, CappedNearMissRecordStatus.FAILED -> return true
                }
                return false
            }
            CappedNearMissRecordStatus.QUEUED -> {
                when (next) {
                    CappedNearMissRecordStatus.RUNNING -> return true
                }
                return false
            }
            CappedNearMissRecordStatus.RUNNING -> return next == CappedNearMissRecordStatus.DONE
            CappedNearMissRecordStatus.DONE -> return next == CappedNearMissRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedNearMissRecordStatus) {
        if (!canMoveTo(next)) {
            error("capped near miss record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped near miss record"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((startsAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): CappedNearMissRecord {
        return CappedNearMissRecord(
            code = code,
            title = title,
            startsAt = startsAt,
            ceiling = ceiling,
            category = category,
            attempts = attempts,
            pallets = pallets,
            tags = tags,
            phone = phone,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped near miss record in status new. */
fun newCappedNearMissRecord(code: String, title: String): CappedNearMissRecord {
    return CappedNearMissRecord(
        code = code,
        title = title,
        startsAt = 0,
        ceiling = 0.0,
        category = "",
        attempts = 0,
        pallets = 0,
        tags = mutableListOf<String>(),
        phone = "",
        dock = "",
        status = CappedNearMissRecordStatus.NEW,
    )
}
